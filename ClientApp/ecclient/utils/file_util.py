# Copyright (C) 2016 Chenfeng Zhu
# 
# This file is part of the "Energy Consumption" project.
# 

import glob
import os
import time
import itertools
from datetime import datetime

from ecclient.conf import properties_reader
from fileinput import filename

FILE_BUFFER_SIZE = 10000

# flag for creating a new file
create_new_file = True

# data file
file_path = properties_reader.getSaveFilePath()
if not file_path.endswith("/"):
    file_path = file_path + "/"
filename_fmt = properties_reader.getSaveFilenameFmt()

# checksum file
checksum_file_path = properties_reader.getSyncFilePath()
if not checksum_file_path.endswith("/"):
    checksum_file_path = checksum_file_path + "/"
checksum_filename_fmt = properties_reader.getSyncFilenameFmt()

device_ids = [] # each device has its own file
datafile_handlers = {}
checksum_s_handlers = {}
checksum_h_handlers = {}

# initialize the parameters
def init(filepath, devIDs):
    global file_path, device_ids
    if (filepath is not None):
        file_path = filepath
    if (devIDs is not None):
        device_ids = devIDs

# create multiple files
def createFiles():
    global create_new_file, filename_fmt, device_ids, datafile_handlers, checksum_s_handlers, checksum_h_handlers
    if create_new_file:
        for devID in device_ids:
            timestamp_long = time.time()
            filename = filename_fmt.replace("[device_id]", devID).replace("[timestamp_long]", str(int(timestamp_long)))
            filepath = file_path + filename
            if os.path.isfile(filepath) and os.access(filepath, os.R_OK):
                print "File exists."
                obj = open(filepath, "a+")
                datafile_handlers[devID] = obj
            else:
                obj = open(filepath, "ab")
                datafile_handlers[devID] = obj
            checksum_filename = checksum_filename_fmt.replace("[device_id]", devID).replace("[timestamp_long]", str(int(timestamp_long)))
            checksum_s_filepath = checksum_file_path + checksum_filename.replace("[type]", "sec")
            checksum_h_filepath = checksum_file_path + checksum_filename.replace("[type]", "hour")
            if os.path.isfile(checksum_s_filepath) and os.access(checksum_s_filepath, os.R_OK):
                obj = open(checksum_s_filepath, "a+")
                checksum_s_handlers[devID] = obj
            else:
                obj = open(checksum_s_filepath, "ab")
                checksum_s_handlers[devID] = obj
            if os.path.isfile(checksum_h_filepath) and os.access(checksum_h_filepath, os.R_OK):
                obj = open(checksum_h_filepath, "a+")
                checksum_h_handlers[devID] = obj
            else:
                obj = open(checksum_h_filepath, "ab")
                checksum_h_handlers[devID] = obj
            create_new_file = False
    else:
        print "Using existing files"
        pass

# Write to the files corresponded to the device
def writeToFile(devID, dataRows):
    global datafile_handlers, checksum_s_handlers
    data_handler = datafile_handlers.get(devID)
    if data_handler != None:
        string = "\n".join(dataRows)
        data_handler.write(string + "\n")
    checksum_handler = checksum_s_handlers.get(devID)
    if checksum_handler != None:
        checksum_handler.write(devID + " checksum")

# Write to multiple files.
def writeToFiles(allData):
    dev_ids = allData.keys()
    for devID in dev_ids:
        dataRows = allData.get(devID)
        writeToFile(devID, dataRows)

# create a single file
def createFile(devID):
    if not file_path.endswith("/"):
        file_path = file_path + "/"
    today = datetime.now().strftime("%Y.%m.%d")
    filename = devID + "_" + today + ".csv"
    obj = open(file_path + filename, "ab", FILE_BUFFER_SIZE)
    datafile_handlers[devID] = obj

#
def write_to_file(values):
    today = datetime.datetime.now().strftime("%Y%m%d")
    filename = "test_" + today + ".csv"
    f = open(filename, "a")
    string = "\n".join(values)
    f.write(string + "\n")
    f.flush()
    f.close()

# Get the latest filename
def getLatestFilename(devID):
    #list_files = os.listdir(file_path)
    #list_datafiles = []
    #for f in list_files:
    #    if devID in f:
    #        list_datafiles.append(f)
    #list_datafiles = sorted(list_datafiles, reverse=True)
    list_files = glob.glob(file_path + devID + "*")
    list_datafiles = sorted(list_files, reverse=True)
    if len(list_datafiles) > 0:
        return os.path.basename(list_datafiles[0])

def getLatestDatetime(devID):
    filename = getLatestFilename(devID)
    return filename.replace(devID + "_", "").replace(".csv", "")

#
def readFromFile(filename, start_line, count_line):
    filepath = file_path + filename
    data_lines = []
    if os.path.isfile(filepath):
        with open(filepath) as fin:
            for line in itertools.islice(fin, start_line, start_line + count_line):
                data_lines.append(line.strip())
        return data_lines
    else:
        print "The file " + filename + " cannot be found."

if __name__ == "__main__":
    devs = ["ABCD", "AHJK"]
    init(None, devs)
    createFiles()
    dataRows = ["20160920150505123456,123.456", "20160920150505654321,654.321"]
    allData = {}
    allData[devs[0]] = dataRows
    allData[devs[1]] = ["20160921150505123456,123.456", "20160921150505654321,654.321"]
    #writeToFile("ABCD", dataRows)
    writeToFiles(allData)
    print getLatestFilename(devs[0])
    print datetime.fromtimestamp(float(getLatestDatetime(devs[0])))
    print readFromFile("ABCD_1474965308.csv", 10, 10)

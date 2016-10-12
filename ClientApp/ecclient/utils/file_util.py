# Copyright (C) 2016 Chenfeng Zhu
# 
# This file is part of the "Energy Consumption" project.
# 

import glob
import os
import time
import itertools
import datetime

from ecclient.conf import properties_reader
from fileinput import filename
from ecclient.utils import checksum_util

FILE_BUFFER_SIZE = 100000000 # for testing
# max signed int: 2^31 -1 = 2147483647
# data in 1 hour: 30 * 44000 * 60 * 60 = 4752000000

CHECKSUM_AVG_LINE_LENGTH = 100 

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

positions = {}

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
            positions[devID] = 0
            create_new_file = False
    else:
        print "Using existing files"
        pass

#
def syncInit(timeStr):
    global device_ids, checksum_h_handlers, datafile_handlers
    for devID in device_ids:
        checksum_filepath = getLatestChecksumFilepath(devID)
        if os.path.isfile(checksum_filepath) and os.access(checksum_filepath, os.R_OK):
            obj = open(checksum_filepath, "a+")
            checksum_h_handlers[devID] = obj
            

# Write to the files corresponded to the device
def writeToFile(devID, dataRows):
    global datafile_handlers, checksum_h_handlers, checksum_s_handlers
    string_data = "\n".join(dataRows)
    data_handler = datafile_handlers.get(devID)
    if data_handler != None:
        data_handler.write(string_data + "\n")
        data_handler.flush()
    checksum_handler = checksum_h_handlers.get(devID)
    if checksum_handler != None:
        # filename,YYYYMMDDhh,position,count,checksum
        count = len(dataRows)
        string_checksum = os.path.basename(data_handler.name) + "," + dataRows[0][0:12] + "," + str(positions[devID]) + "," + str(count) + "," + checksum_util.getMD5(string_data)
        checksum_handler.write(string_checksum + "\n")
        checksum_handler.flush()
        positions[devID] += count
        # print "writing: " + str(checksum_handler) + string_checksum

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
    today = datetime.datetime.now().strftime("%Y.%m.%d")
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

# Get the latest data file path
def getLatestDataFilepath(devID):
    #list_files = os.listdir(file_path)
    #list_datafiles = []
    #for f in list_files:
    #    if devID in f:
    #        list_datafiles.append(f)
    #list_datafiles = sorted(list_datafiles, reverse=True)
    list_files = glob.glob(file_path + devID + "*")
    list_datafiles = sorted(list_files, reverse=True)
    if len(list_datafiles) > 0:
        return list_datafiles[0]
    #raise Exception()
    return None

# Get the latest data filename
def getLatestDataFilename(devID):
    return os.path.basename(getLatestDataFilepath(devID))

# Get the latest checksum file path
def getLatestChecksumFilepath(devID, isHour=True):
    checksum_type = "hour"
    if not isHour:
        checksum_type = "sec"
    list_files = glob.glob(checksum_file_path + devID + "*" + checksum_type + "*")
    list_checksumfiles = sorted(list_files, reverse=True)
    if len(list_checksumfiles) > 0:
        return list_checksumfiles[0]
    #raise Exception()
    return None

# Get the latest checksum filename
def getLatestChecksumFilename(devID):
    return os.path.basename(getLatestChecksumFilepath(devID))

#
def getLatestDatetime(devID):
    filename = getLatestDataFilename(devID)
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

# get the checksum value in the previous hour
def getLatestChecksumLine(devID, timeStr):
    global checksum_h_handlers
    checksum_handler = checksum_h_handlers.get(devID)
    # print checksum_handler
    return tailFile(checksum_handler, 1)[0]

# Reads a n lines from f with an offset of offset lines.
def tailFile(f, n, offset=0):
    avg_line_length = CHECKSUM_AVG_LINE_LENGTH
    to_read = n + offset
    while 1:
        try:
            f.seek(-(avg_line_length * to_read), 2)
        except IOError:
            f.seek(0)
        pos = f.tell()
        lines = f.read().splitlines()
        if len(lines) >= to_read or pos == 0:
            return lines[-to_read:offset and -offset or None]
        avg_line_length *= 1.3

# Get the number of lines in a file
def fileLen(fname):
    i = 0
    with open(fname) as f:
        for i, l in enumerate(f):
            pass
    return i + 1

if __name__ == "__main__":
    devs = ["00aabbccddee", "00aabbccddef"]
    init(None, devs)
    createFiles()
    arr = []
    for i in xrange(0, 44000):
        v = str(20160909151515) + str('%06d'%i) + ":" + str(i)
        arr.append(v)
    dataRows = arr
    allData = {}
    allData[devs[0]] = dataRows
    allData[devs[1]] = ["20160921150505123456,123.456", "20160921150505654321,654.321"]
    #writeToFile("ABCD", dataRows)
    writeToFiles(allData)
    print getLatestDataFilename(devs[0])
    print datetime.datetime.fromtimestamp(float(getLatestDatetime(devs[0])))
    #print readFromFile("ABCD_1474965308.csv", 10, 10)
    syncInit("time_str")
    print getLatestChecksumLine("00aabbccddee", "timeStr")

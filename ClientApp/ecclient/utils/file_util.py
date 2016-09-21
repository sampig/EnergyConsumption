# Copyright (C) 2016 Chenfeng Zhu
# 
# This file is part of the "Energy Consumption" project.
# 

import os
from datetime import datetime

from ecclient.conf import properties_reader

FILE_BUFFER_SIZE = 10000

# flag for creating a new file
create_new_file = True

file_path = properties_reader.getSaveFilePath()
if not file_path.endswith("/"):
    file_path = file_path + "/"
filename_fmt = properties_reader.getSaveFilenameFmt()
device_ids = {} # each device has its own file
file_handlers = {}

# initialize the parameters
def init(filepath, devIDs):
    global file_path, device_ids
    if (filepath is not None):
        file_path = filepath
    if (devIDs is not None):
        device_ids = devIDs

# create multiple files
def createFiles():
    global create_new_file, filename_fmt, device_ids
    if create_new_file:
        for devID in device_ids:
            filename = filename_fmt.replace("[device_id]", devID).replace("[yyyymmdd]", str(datetime.now())[0:10])
            filepath = file_path + filename
            if os.path.isfile(filepath) and os.access(filepath, os.R_OK):
                print "File exists."
                obj = open(filepath, "a+")
                file_handlers[devID] = obj
            else:
                obj = open(filepath, "ab")
                file_handlers[devID] = obj
            create_new_file = False
    else:
        print "Using existing files"
        pass

# create a single file
def createFile(devID):
    global file_path
    if not file_path.endswith("/"):
        file_path = file_path + "/"
    today = datetime.now().strftime("%Y.%m.%d")
    filename = devID + "_" + today + ".csv"
    obj = open(file_path + filename, "ab", FILE_BUFFER_SIZE)
    file_handlers[devID] = obj
    
#
def writeToFile(devID, dataRows):
    global create_new_file, file_handlers
    handler = file_handlers.get(devID)
    if handler != None:
        string = "\n".join(dataRows)
        handler.write(string + "\n")

#
def write_to_file(values):
    today = datetime.datetime.now().strftime("%Y%m%d")
    filename = "test_" + today + ".csv"
    f = open(filename, "a")
    string = "\n".join(values)
    f.write(string + "\n")
    f.flush()
    f.close()

#
def readFromFile(devID):
    pass

if __name__ == "__main__":
    init(None, {"ABCD", "AHJK"})
    createFiles()
    dataRows = ["20160920150505123456,123.456", "20160920150505654321,654.321"]
    writeToFile("ABCD", dataRows)

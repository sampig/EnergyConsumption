# Copyright (C) 2016 Chenfeng Zhu
# 
# This file is part of the "Energy Consumption" project.
# 

from datetime import datetime

FILE_BUFFER_SIZE = 10000

# flag for creating a new file
create_new_file = False

filePath = "."
deviceIDs = {} # each device has its own file
fileWriters = {}
fileHandlers = {}

# initialize the parameters
def init(filepath, devIDs):
    global filePath, deviceIDs
    filePath = filepath
    deviceIDs = devIDs

# create multiple files
def createFiles():
    if len(fileWriters) != 0:
        for key in fileWriters.keys():
            fileHandlers[key].close()

# create a single file
def createFile(devID):
    global filePath
    if not filePath.endswith("/"):
        filePath = filePath + "/"
    today = datetime.now().strftime("%Y.%m.%d")
    filename = devID + "_" + today + ".csv"
    obj = open(filePath + filename, 'ab', FILE_BUFFER_SIZE)
    fileHandlers[devID] = obj
    

def writeToFile(devID, dataRows):
    global create_new_file, fileWriters
    writer = fileWriters.get(devID)
    if writer != None:
        dataRows.size


def write_to_file(values):
    today = datetime.datetime.now().strftime("%Y%m%d")
    filename = "test_" + today + ".csv"
    f = open(filename, 'a')
    string = '\n'.join(values)
    f.write(string + "\n")
    f.flush()
    f.close()
    

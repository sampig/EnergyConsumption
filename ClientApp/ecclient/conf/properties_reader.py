# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
# Make sure that the "config.properties" file is in the same directory.
#

import ConfigParser
import os

config = ConfigParser.RawConfigParser()
configFile = os.path.join(os.path.abspath(os.path.dirname(__file__)), "config.properties")
config.read(configFile)

def getSaveFrequency():
    freq = config.getint("Transmit","transmit.savefreq")
    if freq <= 0:
        freq = 30
    if freq > 60:
        freq = 60
    return freq

def getSaveFilePath():
    filepath = config.get("Transmit","transmit.savefilepath")
    return filepath

def getSaveFilenameFmt():
    file_fmt = config.get("Transmit","transmit.savefilefmt")
    return file_fmt

# Get hostname and port of Ostinato
def getOstinatoConfigHost():
    hostname = config.get("Ostinato", "ostinato.hostname")
    port = config.getint("Ostinato", "ostinato.port")
    return [hostname, port]

# Get the file of device information
def getDevicesFileName():
    filename = config.get("Files", "file.devices")
    return filename

# Get the URI of SIP Server
def getSIPServerURI():
    serveruri = config.get("SIP", "sip.serveruri")
    return serveruri

def getSyncFilePath():
    filepath = config.get("Sync","sync.savefilepath")
    return filepath

def getSyncFilenameFmt():
    file_fmt = config.get("Sync","sync.savefilefmt")
    return file_fmt

def getSyncServer():
    ip = config.get("Sync","sync.serverip")
    port = config.getint("Sync","sync.serverport")
    return (ip, port)

if __name__ == "__main__":
    print "Save Filename Format: ", getSaveFilenameFmt()
    print "Ostinato Config: ", getOstinatoConfigHost()
    print "SIP Server URI: ", getSIPServerURI()
    print "Sync Server: ", getSyncServer()


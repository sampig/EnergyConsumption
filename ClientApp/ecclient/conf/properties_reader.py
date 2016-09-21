# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
# Make sure that the "config.properties" file is in the same directory.
#

import ConfigParser
import os

config = ConfigParser.RawConfigParser()
configFile = os.path.join(os.path.abspath(os.path.dirname(__file__)), 'config.properties')
config.read(configFile)

def getSaveFrequency():
    freq = config.get("Transmit","transmit.savefreq")
    return freq

def getSaveFilePath():
    filepath = config.get("Transmit","transmit.savefilepath")
    return filepath

def getSaveFilenameFmt():
    file_fmt = config.get("Transmit","transmit.savefilefmt")
    return file_fmt

def getSyncFilePath():
    filepath = config.get("Sync","sync.savefilepath")
    return filepath

def getSyncFilenameFmt():
    file_fmt = config.get("Sync","sync.savefilefmt")
    return file_fmt

# Get hostname and port of Ostinato
def getOstinatoConfigHost():
    hostname = config.get('Ostinato', 'ostinato.hostname')
    port = config.getint('Ostinato', 'ostinato.port')
    return [hostname, port]

# Get the file of device information
def getDevicesFileName():
    filename = config.get('Files', 'file.devices')
    return filename

# Get the URI of SIP Server
def getSIPServerURI():
    serveruri = config.get('SIP', 'sip.serveruri')
    return serveruri


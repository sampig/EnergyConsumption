# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
# Make sure that the "config.properties" file is in the top directory of this project.
#

import ConfigParser
import os

config = ConfigParser.RawConfigParser()
configFile = os.path.join(os.path.abspath(os.path.dirname(__file__)), 'config.properties')
config.read(configFile)

# Get hostname and port of Ostinato
def getOstinatoConfigHost():
    hostname = config.get('Ostinato', 'ostinato.hostname')
    port = int(config.get('Ostinato', 'ostinato.port'))
    return [hostname, port]

# Get the file of device information
def getDevicesFileName():
    filename = config.get('Files', 'file.devices')
    return filename

# Get the URI of SIP Server
def getSIPServerURI():
    serveruri = config.get('SIP', 'sip.serveruri')
    return serveruri


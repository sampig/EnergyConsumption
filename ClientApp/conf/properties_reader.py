# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
# Make sure that the "config.properties" file is in the top directory of this project.
#

import ConfigParser

config = ConfigParser.RawConfigParser()
config.read('config.properties')

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



# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#
# This file is used to get all devices to save time.
#

from conf import properties_reader


def getDevices():
    filename = properties_reader.getDevicesFileName()
    
    mac_addresses = []
    device_names = {}

    devices = open(filename, 'rb')

    # Extract MAC and device identity from mapping file
    for line in devices.readlines():
        device_attributes = line.split("|")
        mac = device_attributes[0].strip()
        name = device_attributes[1:]
        name = "_".join(name).strip()
        if name == '':
            name = "dev_" + mac

        mac_addresses.append(mac)
        device_names[mac] = name

    return (mac_addresses, device_names)


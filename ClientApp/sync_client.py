# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import datetime
import socket
import time

from ecclient.conf import properties_reader
from ecclient.utils import devices_reader, file_util

SLEEP_TIME = 5 # 60

BUFFER_SIZE = 1024

_running = True

server = properties_reader.getSyncServer()

# connect to the server
def connectToServer():
    print "Trying to connect to Sync server..."
    tcp_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        tcp_sock.connect(server)
    except:
        return None
    return tcp_sock

def __getLatestChecksumValue(devID, timeStr):
    file_util.syncInit(timeStr)
    line = file_util.getLatestChecksumLine(devID, timeStr)
    # filename,YYYYMMDDhh,position,count,checksum
    return line

def startSync():
    
    mac_addresses = devices_reader.getDevices()[0]
    
    now = datetime.datetime.now()
    time_check = now.strftime("%Y%m%d%H%M%S")
    
    file_util.init(None, mac_addresses)
    file_util.syncInit(time_check)
    
    while _running:
        now = datetime.datetime.now()
        time_str = now.strftime("%Y%m%d%H%M%S")
        hour_str = time_str[0:14]
        if time_check != hour_str:
            while _running:
                tcp_sock = connectToServer()
                if tcp_sock is None:
                    time.sleep(SLEEP_TIME)
                    continue
                else:
                    break
            print "Starting sync..."
            
            for devID in mac_addresses:
                # get the checksum value in the previous hour
                checksum_value = __getLatestChecksumValue(devID, time_str)
                tcp_sock.send(checksum_value)
            
            tcp_sock.send("TCP_MSG 1")
            reply_data = tcp_sock.recv(BUFFER_SIZE)
            print reply_data
            tcp_sock.send("TCP_MSG 2")
            reply_data = tcp_sock.recv(BUFFER_SIZE)
            print reply_data
            tcp_sock.send("finish")
            while True:
                reply_data = tcp_sock.recv(BUFFER_SIZE)
                print reply_data
                if reply_data == "finish":
                    break
            tcp_sock.close()
        else:
            time.sleep(SLEEP_TIME)

def stopSync():
    _running = True
    print "Stopping sync..."

if __name__ == "__main__":
    startSync()

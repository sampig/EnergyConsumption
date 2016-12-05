# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import datetime
import socket
import time
import sys

from ecclient.conf import properties_reader
from ecclient.utils import devices_reader, file_util

SYNC_FREQ_MIN = properties_reader.getSyncFrequency()

SLEEP_TIME = 5  # 60

BUFFER_SIZE = 10240

_running = True

server = properties_reader.getSyncServer()

def logStr():
    return "Sync Client [" + time.strftime("%Y-%m-%d %H:%M:%S") + "]: "

# connect to the server
def connectToServer():
    print logStr(), "Trying to connect to Sync server..."
    tcp_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        tcp_sock.connect(server)
    except:
        return None
    print logStr(), "Connected to Sync server", server
    return tcp_sock

def __getLatestChecksumValue(devID, timeStr):
    file_util.syncInit(timeStr)
    line = file_util.getLatestChecksumHLine(devID, timeStr)
    # print "__getLatestChecksumValue:", line
    # YYYYMMDDhhmiss,count,checksum,start_position,filename
    return line

def __getLatestChecksumSecValues(devID, timeStr):
    file_util.syncInit(timeStr)
    lines = file_util.getLatestChecksumSLines(devID, timeStr)
    return lines

def __getAllData(devID, timeStr, filename):
    file_util.setSyncDatafile(devID, filename)
    all_data = file_util.readDataFromFile(devID, timeStr)
    return all_data

def startSync():
    global _running

    mac_addresses = devices_reader.getDevices()[0]
    dev_id = mac_addresses[0]

    now = datetime.datetime.now()
    time_check = now.strftime("%Y%m%d%H")

    file_util.init(None, {dev_id})
    file_util.syncInit(time_check)

    freq_sync_sec = SYNC_FREQ_MIN * 60

    while _running:
        now = datetime.datetime.now()
        # now_str = now.strftime("%Y%m%d%H%M%S")
        now_t = time.time()
        s_check = int(now_t)
        if s_check % freq_sync_sec == 0:
            time_str = (now - datetime.timedelta(minutes=30)).strftime("%Y%m%d%H%M%S")
            time.sleep(SLEEP_TIME)
            while _running:
                tcp_sock = connectToServer()
                if tcp_sock is None:
                    time.sleep(SLEEP_TIME)
                    continue
                else:
                    break
            print logStr(), "Starting SYNC...", (dev_id + "|" + time_str)
            tcp_sock.send("SyncStart|" + dev_id + "|" + time_str)

            reply = tcp_sock.recv(BUFFER_SIZE)
            print logStr(), "Ready confirmed.", reply

            # get the checksum value in the previous period
            checksum_value = __getLatestChecksumValue(dev_id, time_str)
            while checksum_value is None:
                time.sleep(SLEEP_TIME)
                checksum_value = __getLatestChecksumValue(dev_id, time_str)
            print logStr(), "Send checksum to server.", checksum_value
            tcp_sock.send(checksum_value)

            # receive the comparison result in the previous period
            result_h = tcp_sock.recv(BUFFER_SIZE)
            print logStr(), "Receive result: ", result_h

            if "H_SAME" in result_h:  # the result is the same
                print logStr(), time_str, "is the same.", dev_id
            elif "H_DIFF" in result_h:
                print logStr(), time_str, "is different.", dev_id
                # get the checksum value for seconds in the previous period
                # send the checksum values one by one
                lines = __getLatestChecksumSecValues(dev_id, time_str)
                all_data = __getAllData(dev_id, time_str, checksum_value.split("|")[4])
                for line in lines:
                    tcp_sock.send(line)
                    result_s = tcp_sock.recv(BUFFER_SIZE)
                    if "S_SAME" in result_s:  # the result is the same
                        # print logStr(), time_str, "is the same.", dev_id
                        continue
                    elif "S_DIFF" in result_s:
                        sec = lines.split("|")[0]
                        print logStr(), sec, "(s) is different.", dev_id
                        datas = all_data.get(sec)
                        print logStr(), "Send data to server.", sec, len(datas)
                        tcp_sock.send(datas)
                        tcp_sock.recv(BUFFER_SIZE)
                    else:
                        print logStr(), "Unknown comparison S result.", line
            else:
                print logStr(), "Unknown comparison H result."

            # checksum_values = "".join(lines)
            # print logStr(), "Send second checksum to server.", dev_id, time_str, len(lines), len(checksum_values)
            # tcp_sock.send(checksum_values)

            # receive the comparison result for seconds
            tcp_sock.send("SyncEnd|" + dev_id + "|" + time_str)
            print logStr(), "Send SyncEnd to server."
            final_reply = tcp_sock.recv(BUFFER_SIZE)
            print logStr(), "Stop SYNC...", final_reply
            tcp_sock.close()
        else:
            time.sleep(1)

''' Stop synchronization '''
def stopSync():
    global _running
    _running = False
    print "Stopping sync..."
    sys.exit()

if __name__ == "__main__":
    startSync()

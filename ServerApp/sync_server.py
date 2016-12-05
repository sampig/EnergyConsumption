# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import datetime
import socket
import time
import struct

from ecserver.conf import properties_reader
from ecserver.utils import cassandra_util
from ecserver.utils import checksum_util

BUFFER_SIZE = 10240

SLEEP_TIME = 5  # 60

host = properties_reader.getSyncHost()

SYNC_FREQ_MIN = properties_reader.getSyncFreq()

db_connection = None
db_manager = None

def logStr():
    return "Sync Server [" + time.strftime("%Y-%m-%d %H:%M:%S") + "]: "

def startListen():
    tcp_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    tcp_sock.bind(host)
    tcp_sock.listen(1)
    print logStr(), "Starting listening on ", host
    return tcp_sock

# YYYYMMDDhhmiss
def __calChecksum(devID, timeStr):
    # calculate the hour checksum value
    start_min = timeStr[8:14]
    end_min = datetime.datetime.fromtimestamp(time.mktime(time.strptime(timeStr, "%Y%m%d%H%M%S")) + 1800).strftime("%Y%m%d%H%M%S")[8:14]
    print logStr(), "Start calculating the checksum...", devID, timeStr, str(start_min), str(end_min)
    count = 0
    all_data = []
    second_data = {}
    for i in xrange(int(start_min), int(end_min)):
        second_data[i] = []

    all_data_rows = db_manager.getAllConsumptionP(devID, timeStr)
    for row in all_data_rows:
        hm = row.ec_time
        c = 0
        if hm >= start_min and hm < end_min:
            sec = time.mktime(time.strptime(row.ec_date + row.ec_time, "%Y%m%d%H%M%S"))
            values = row.ec_consumption_values
            vc = row.values_count
            for i in xrange(0, int(len(values) / 9)):
                v = values[i:(i + 1) * 9]
                if len(v) < 9:
                    continue
                us = struct.unpack("i", v[0:4])[0]
                r = struct.pack("d", (int(sec) + us / 1000000.0)) + v[4:8]
                all_data.append(r)
                second_data[int(hm)].append(r)
                c += 1
                count += 1
        if vc != c:
            # update
            pass
    total_count = len(all_data)
    checksum_p = checksum_util.getMD5("\n".join(all_data) + "\n")
    db_manager.insertChecksumP(devID, timeStr, checksum_p, total_count)
    print logStr(), "Inserting the checksum P...", devID, timeStr, checksum_p, total_count
    for k in sorted(second_data.keys()):
        timestamp = timeStr[0:8] + hm.zfill(6) # datetime.datetime.fromtimestamp(k).strftime("%Y%m%d%H%M%S")
        data = second_data[k]
        checksum_s = checksum_util.getMD5("\n".join(data) + "\n")
        db_manager.insertChecksumS(devID, timestamp, checksum_s, len(data))
    print logStr(), "Inserting the checksum S...", devID, timeStr, len(second_data)

def __updateData(devID, timeSec, dataSec):
    print logStr(), timeSec, "(s) is different. Updating data...", devID
    db_manager.deleteConsumption(devID, timeSec)
    db_manager.insertConsumptionRaw(devID, timeSec, dataSec, "\n")

def __updateChecksumSecValue(devID, timeStr, dataSec):
    print logStr(), timeStr, "(s) is different. Updating sec checksum...", devID

def __updateChecksumPValue(devID, timeStr):
    print logStr(), timeStr, "(s) is different. Updating period checksum...", devID

def startSync():
    global db_connection, db_manager

    db_connection = cassandra_util.CassandraConnection()
    db_manager = cassandra_util.CassandraManager(db_connection)

    tcp_sock = startListen()

    now = datetime.datetime.now()
    time_check = now.strftime("%Y%m%d%H%M%S")

    freq_sync_sec = SYNC_FREQ_MIN * 60

    while True:
        # try:
            conn, addr = tcp_sock.accept()

            # start sync
            msg_start = conn.recv(BUFFER_SIZE)
            dev_id = msg_start.split("|")[1]
            time_str = msg_start.split("|")[2]
            print logStr(), "Starting SYNC...{", (dev_id + "|" + time_str)

            # get latest checksum value
            checksum_h_row = None
            while True:
                checksum_h_rows = db_manager.getChecksumPeriodValue(dev_id, time_str)
                if checksum_h_rows is None:
                    time.sleep(SLEEP_TIME)
                    __calChecksum(dev_id, time_str)
                    continue
                else:
                    for row in checksum_h_rows:
                        checksum_h_row = row
                        break
                    if checksum_h_row is not None:
                        break
                    else:
                        __calChecksum(dev_id, time_str)

            conn.send("SyncReady|" + dev_id + "|" + time_str)
            print logStr(), "Sync Ready.{", (dev_id + "|" + time_str)

            # compare the data in the previous period
            checksum_h_value = conn.recv(BUFFER_SIZE).split("|")  # time|count|checksum|start|filename
            print logStr(), "Server P:", checksum_h_row.values_checksum
            print logStr(), "Client P:", checksum_h_value[2]
            if checksum_h_row.values_count == checksum_h_value[1] and checksum_h_row.values_checksum == checksum_h_value[2]:
                conn.send("H_SAME")
                print logStr(), time_str, "(p) is the same.", dev_id
            else:
                conn.send("H_DIFF")
                print logStr(), time_str, "(p) is different.", dev_id
                # get the checksum value for seconds in the previous period
                # compare the checksum values one by one
                while True:
                    checksum_s_value = conn.recv(BUFFER_SIZE).split("|")  # time|count|checksum
                    print logStr(), "receiving ", checksum_s_value
                    sec = datetime.datetime.fromtimestamp(float(checksum_s_value[0])).strftime("%Y%m%d%H%M%S")
                    checksum_s_rows = db_manager.getChecksumSecValue(dev_id, sec)
                    if checksum_s_rows is None:
                        conn.send("S_DIFF")
                    else:
                        checksum_s_row = None
                        for row in checksum_s_rows:
                            checksum_s_row = row
                            break
                        if checksum_s_row is None:
                            continue
                        if checksum_s_row.values_count == checksum_s_value[1] and checksum_s_row.values_checksum == checksum_s_value[2]:
                            # if this second is the same, receive the next second,
                            conn.send("S_SAME")
                            print logStr(), time_str, "(s) is the same.", dev_id
                            continue
                        else:
                            conn.send("S_DIFF")
                    data_sec = conn.recv(BUFFER_SIZE)
                    print logStr(), sec, "(s) is different. Updating...", dev_id
                    __updateData(dev_id, sec, data_sec)
                    __updateChecksumSecValue(dev_id, sec, data_sec)
                    conn.send("S_DONE")

                __updateChecksumPValue(dev_id, time_str)

            msg_end = conn.recv(BUFFER_SIZE)
            print logStr(), "Sync End.", msg_end
            conn.send("SyncEnd")
            print logStr(), "Stop SYNC...", (dev_id + "|" + time_str)
            conn.close()

            time.sleep(freq_sync_sec * 0.9)
        # except:
        #    print __file__ + ": Unexpected error:", sys.exc_info()[0]
        #    print __file__ + ": Sync Server will restart in " + str(SLEEP_TIME) + "seconds..."
        #    time.sleep(SLEEP_TIME)
            # tcp_sock = startListen()
        #    continue
    tcp_sock.close()

if __name__ == "__main__":
    startSync()

'''
connection = CassandraConnection()
session = connection.getSession()
print connection.getCluset().get_core_connections_per_host(0)#.get_max_connections_per_host(0)
rows = session.execute('select * from ec_consumption LIMIT 10')

for row in rows:
    print row.device_id.strip(' \t\n\r'),"\t| ", row.ec_date.strip() , "\t| ", str(row.ec_consumption_values)
'''

'''
import socket
from time import gmtime, strftime

HOST = '127.0.0.1'
TCP_PORT = 7979

BUFFER_SIZE = 64

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((HOST, TCP_PORT))
s.listen(1)
conn, addr = s.accept()
print 'Connection address:', addr

while True:
    data = conn.recv(BUFFER_SIZE)
    if not data: continue
    s.close()
    print "received TCP message: <", str(data), "> at %s" % strftime("%Y-%m-%d %H:%M:%S +0000", gmtime())
    conn.send(data)

conn.close()
'''

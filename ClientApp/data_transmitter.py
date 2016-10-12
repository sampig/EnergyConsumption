# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import time
import socket
import struct
import binascii
import datetime
import threading
import sys

# from ecclient.conf import properties_reader
from ecclient.utils import devices_reader, file_util, sip_config

_running = True

''' handle with packet header '''
def processEth(data):
    # Ehternet II: destination mac, source mac, type
    smac = str(binascii.hexlify(data[1]))
    dmac = None  # str(binascii.hexlify(data[0]))
    ethtype = None  # str(binascii.hexlify(data[2]))
    return [smac, dmac, ethtype]

def processIP(data):
    # IPv4: ..., source ip, destination ip
    sip = str(socket.inet_ntoa(data[1]))
    dip = str(socket.inet_ntoa(data[2]))
    return [sip, dip]

def processPort(data):
    # UDP:
    sport = None  # data[0]
    dport = data[1]
    return [sport, dport]

''' connect to SIP server '''
def sipConnect():
    global buddy
    print "Connected to sip"
    buddy = sip_config.connect()
    while not buddy:
        time.sleep(1)
    buddy.send_typing_ind(True)

''' send data '''
def sendData(dev_id, time_str, data_arr, start_pos):
    msg_str = dev_id + "|" + time_str + "|" + str(";".join(data_arr[start_pos:]))
    # data format: dev_id|YYYYMMDDhhmissSSSSSS|SSSSSS,value;SSSSSS,value;...
    # print "sending " + str(len(data_arr[start_pos:])) + " packets of " + dev_id + " to server..." + str(start_pos)
    buddy.send_pager(msg_str)

''' write data into file'''
def writeData(dev_id, time_str, data_list):
    data_rows = []
    time_second = time_str[0:14]
    for t in sorted(data_list.keys()):
        for d in data_list[t]:
            data_rows.append(time_second + d)
    print "writing into file..." + str(len(data_rows))
    file_util.writeToFile(dev_id, data_rows)

def transmitData():

    mac_addresses = devices_reader.getDevices()[0]

    sipConnect()

    file_util.init(None, mac_addresses)
    file_util.createFiles()
    # num_devices = len(mac_addresses)
    # save_freq = properties_reader.getSaveFrequency()

    total_values = {}
    time_marks = {}
    time_us_check = {}  # for sending
    time_s_check = {}
    time_hour_check = {}  # for writing
    start_pos = {}
    now = datetime.datetime.now()
    us_str = now.strftime("%Y%m%d%H%M%S%f")
    # pre-load devices (To be REMOVED)
    for key in mac_addresses:
        total_values[str(key)] = {}
        time_marks[str(key)] = []
        time_us_check[str(key)] = us_str[0:17]
        time_s_check[str(key)] = None
        time_hour_check[str(key)] = us_str[0:10]  #
        start_pos[str(key)] = 0
    time_values = {}

    sock = socket.socket(socket.PF_PACKET, socket.SOCK_RAW, socket.htons(0x800))
    print "\n\nReceiving data from devices...\n"

    # count = 0
    while _running:  # len(pkt) > 0:

        try:
            pkt = sock.recv(65536)
        except:
            continue

        try:
            if(len(pkt)) > 54:
                # handle with the header of the packet, useless parts are removed.
                ethHeader = pkt[0:14]  # eth: 14
                # ipHeader = pkt[14:34]  # ip: 20
                portHeader = pkt[34:38]  # tcp/udp: 8
                # data = pkt[42:47]  # payload
                # ethH = struct.unpack("!6s6s2s", ethHeader)
                # ethdata = processEth(ethH)
                # ipH = struct.unpack("!12s4s4s", ipHeader)
                # ipdata = processIP(ipH)
                tcpudpH = struct.unpack("!HH16", portHeader)
                portdata = processPort(tcpudpH)
    
                if portdata[1] == 17878:  # in dst_ports:  #
    
                    now = datetime.datetime.now()
                    ethH = struct.unpack("!6s6s2s", ethHeader)
                    ethdata = processEth(ethH)
                    # dataC = struct.unpack("!3s2s", data)
    
                    m = ethdata[0]  # mac - dev_id
                    # if m not in mac_addresses:
                    #    continue
                    #    mac_addresses.append(m)
                    #    total_values[m] = {}
                    #    time_marks[m] = []
                    time_values = total_values[m]
    
                    us_str = now.strftime("%Y%m%d%H%M%S%f")  # timestamp
                    h_check = us_str[0:10]  # YYYYMMDDhh
                    second = us_str[0:14]  # YYYYMMDDhhmiss
                    us_check = us_str[0:17]
    
                    if time_hour_check[m] != h_check:
                        # start a thread to write data
                        # writeData(m, us_str, time_values)
                        sendData(m, us_str, time_values[time_us_check[m][0:14]], start_pos[m])
                        t = threading.Thread(target=writeData, args=(m, us_str, time_values))
                        t.start()
                        time_hour_check[m] = h_check
                        time_us_check[m] = us_check
                        time_s_check[m] = second
                        start_pos[m] = 0
                        total_values[m] = {}
                        time_values = total_values[m]
                        time_values[second] = []
                    elif time_s_check[m] != second:  # second not in time_marks[m]:
                        # save data to next second
                        # time_marks[m].append(second)
                        time_values[second] = []
                        sendData(m, us_str, time_values[time_us_check[m][0:14]], start_pos[m])
                        #threading.Thread(target=sendData, args=(m, us_str, time_values[time_us_check[m][0:14]], start_pos[m])).start()
                        time_us_check[m] = us_check
                        time_s_check[m] = second
                        start_pos[m] = 0
                    elif time_us_check[m] != us_check:
                        # send data
                        sendData(m, us_str, time_values[time_us_check[m][0:14]], start_pos[m])
                        #threading.Thread(target=sendData, args=(m, us_str, time_values[time_us_check[m][0:14]], start_pos[m])).start()
                        time_us_check[m] = us_check
                        start_pos[m] = len(time_values[time_us_check[m][0:14]])
    
                    # v = str(int(binascii.hexlify(dataC[0]), 16) + int(binascii.hexlify(dataC[1]), 16) / 10000.0)
                    value = us_str[14:20] + ",1234.567"  # SSSSSS,value
    
                    time_values[second].append(value)
    
                    # print "From Smac(" + ethdata[0] + ") Sip(" + ipdata[0] + ":" + str(portdata[0]) + ") to Dmac(" + ethdata[1] + ") Dip(" + ipdata[1] + ":" + str(portdata[1]) + ")"
                    # print str(len(data)) + " data:" + str(value)
                    # count += 1
                # if count == 1:
                #    start = datetime.datetime.now()
                # elif count >= 44000:
                #    print datetime.datetime.now() - start
                #    break
    
            else:
                continue
        except:
            print "Unexpected error:", sys.exc_info()[0]
            continue


    # stopDataGeneration()

    # print total_values.keys()

    # for key in total_values.keys():
    #    for k in sorted(time_values.keys()):
    #        time_values = total_values[key]
    #        values = time_values[k]
    #        print key + ", " + k + ": " + str(len(values))

def stopTransmission():
    print "Stopping transmission..."
    _running = False

'''
Useless testing
'''
def transmitPseudoData():

    mac_addresses = devices_reader.getDevices()[0]

    sipConnect()

    file_util.init(None, mac_addresses)
    file_util.createFiles()

    start = datetime.datetime.now()
    total_values = {}
    time_marks = {}
    time_us_check = {}  # for sending
    time_s_check = {}
    time_hour_check = {}  # for writing
    start_pos = {}
    now = datetime.datetime.now()
    us_str = now.strftime("%Y%m%d%H%M%S%f")
    for key in mac_addresses:
        total_values[str(key)] = {}
        time_marks[str(key)] = []
        time_us_check[str(key)] = us_str[0:17]
        time_s_check[str(key)] = "0"  # us_str[0:14]
        time_hour_check[str(key)] = us_str[0:14]
        start_pos[str(key)] = 0
    time_values = {}
    for x in xrange(0, 44000 * 2):
        now = datetime.datetime.now()
        m = mac_addresses[x % 2]
        time_values = total_values[m]

        us_str = now.strftime("%Y%m%d%H%M%S%f")  # timestamp
        h_check = us_str[0:14]  # YYYYMMDDhh
        second = us_str[0:14]  # YYYYMMDDhhmiss
        us_check = us_str[0:17]
        u_second = us_str[14:20]  # SSSSSS

        if time_s_check[m] != second:
            # save data to next second and send data
            time_values[second] = []
            sendData(m, us_str, time_values[time_us_check[m][0:14]], start_pos[m])
            time_us_check[m] = us_check
            time_s_check[m] = second
            start_pos[m] = 0
        elif time_us_check[m] != us_check:
            # send data
            sendData(m, us_str, time_values[time_us_check[m][0:14]], start_pos[m])
            time_us_check[m] = us_check
            start_pos[m] = len(time_values[time_us_check[m][0:14]])
        if time_hour_check[m] != h_check:
            # write data
            threading.Thread(target=writeData, args=(m, us_str, time_values)).start()
            time_hour_check[m] = h_check

        # v = str(int(binascii.hexlify(dataC[0]), 16) + int(binascii.hexlify(dataC[1]), 16) / 10000.0)
        value = u_second + ",1234.567"  # SSSSSS,value

        time_values[second].append(value)
    now = datetime.datetime.now()
    print now - start



if __name__ == "__main__":
    transmitPseudoData()




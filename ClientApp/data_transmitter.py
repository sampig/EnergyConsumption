# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import time
import socket
import struct
import binascii
import datetime
import signal
import threading
import sys

from data_generator import DataGeneratorOstinato
from utils import devices_reader

''' data generation '''
def startDataGeneration(num=1):
    global dataGen
    dataGen = DataGeneratorOstinato()
    dataGen.create_stream(num)

    i = 0
    num_packets = 1  # 000
    packets_per_sec = num * 44000  # 000
    #src_mac = int(mac_addresses[i], 16) #0x00aabbccddee 733295205870
    dst_mac = 0x000000000000
    src_ip = 0x01020304
    dst_ip = 0x7f000001
    src_port = 80
    dst_port = 17878
    print "Starting data generation by Ostinato..."
    while i < num:
        try:
            if i == num - 1:
                dataGen.setStreamcontrolParameters(num_packets, packets_per_sec, False)
            else:
                dataGen.setStreamcontrolParameters(num_packets, packets_per_sec, True)
            src_mac = int(mac_addresses[i], 16)
            dataGen.setProtocolSourceParameters(src_mac, src_ip, src_port)
            dataGen.setProtocolDestinationParameters(dst_mac, dst_ip, dst_port)
            dataGen.add_stream(i)
            #dst_ports.append(dst_port)
            i += 1
            #src_mac += 1#src_mac = int(mac_addresses[i], 16) #
            #dst_port += 1
        except:
            print "Error"
            # dataGen.stream_stop()
            exit
    dataGen.start_stream()

def stopDataGeneration():
    time.sleep(5)
    print "Stoping data generation..."
    try:
        dataGen.stop_stream()
        dataGen.disconnect()
        sys.exit(0)
    except:
        pass
    finally:
        sys.exit(0)

''' handle with packet header '''
def processEth(data):
    # Ehternet II: destination mac, source mac, type
    smac = str(binascii.hexlify(data[1]))
    dmac = None #str(binascii.hexlify(data[0]))
    ethtype = None #str(binascii.hexlify(data[2]))
    return [smac, dmac, ethtype]

def processIP(data):
    # IPv4: ..., source ip, destination ip
    sip = str(socket.inet_ntoa(data[1]))
    dip = str(socket.inet_ntoa(data[2]))
    return [sip, dip]

def processPort(data):
    # UDP:
    sport = None #data[0]
    dport = data[1]
    return [sport, dport]

''' handle with ctrl+c '''
def sigint_handler(signum, frame):
    stopDataGeneration()
    print 'Stop pressing the CTRL+C!'

signal.signal(signal.SIGINT, sigint_handler)


'''
##########################################################################
### Data Generation
##########################################################################
'''

mac_addresses = devices_reader.getDevices()[0]
num_devices = len(mac_addresses)
#dst_ports = []
startDataGeneration(num_devices)
time.sleep(1)

total_values = {}
time_marks = {}
for key in mac_addresses:
    total_values[str(key)] = {}
    time_marks[str(key)] = []
time_values = {}
values = []

#now = datetime.datetime.now()
#for key in mac_addresses:
#    time_values = total_values[str(key)]
#    for i in xrange(0, 20):
#        tmp = now + datetime.timedelta(0,i)
#        s = tmp.strftime("%Y%m%d%H%M%S")
#        time_marks[str(key)].append(s[0:14])
#        time_values[s[0:14]] = []

#print mac_addresses
#print total_values

threading.Thread(target=stopDataGeneration, args=[]).start()

'''
##########################################################################
### Main
##########################################################################
'''

sock = socket.socket(socket.PF_PACKET, socket.SOCK_RAW, socket.htons(0x800))
print "Waiting for receiving..."
# try:
#     pkt = sock.recv(65536)
# except:
#     pass

count = 0
while True: # len(pkt) > 0:

    try:
        pkt = sock.recv(65536)
    except:
        break
    
    now = datetime.datetime.now()

    if(len(pkt)) > 54:
        ethHeader = pkt[0:14]  # eth: 14
        ipHeader = pkt[14:34]  # ip: 20
        portHeader = pkt[34:38]  # tcp/udp: 8
        data = pkt[42:47]  # payload

        # ethH = struct.unpack("!6s6s2s", ethHeader)
        # ethdata = processEth(ethH)
        # ipH = struct.unpack("!12s4s4s", ipHeader)
        # ipdata = processIP(ipH)
        tcpudpH = struct.unpack("!HH16", portHeader)
        portdata = processPort(tcpudpH)

        # print "From Smac(" + ethdata[0] + ") Sip(" + ipdata[0] + ":" + portdata[0] + ") to Dmac(" + ethdata[1] + ") Dip(" + ipdata[1] + ":" + portdata[1] + ")"

        if portdata[1] == 17878: # in dst_ports:  # 
            ethH = struct.unpack("!6s6s2s", ethHeader)
            ethdata = processEth(ethH)
            dataC = struct.unpack("!3s2s", data)

            m = ethdata[0]
            us = now.strftime("%Y%m%d%H%M%S%f")
            s = us[0:14]
            # t = now.strftime("%Y-%m-%d %H:%M:%S.%f")
            v = str(int(binascii.hexlify(dataC[0]), 16) + int(binascii.hexlify(dataC[1]), 16) / 10000.0)
            value = us + "|" + m + "|" + v
            
            #if m not in mac_addresses:
            #    mac_addresses.append(m)
            #    total_values[m] = {}
            #    time_marks[m] = []
            time_values = total_values[m]
            if s not in time_marks[m]:
                time_marks[m].append(s)
                time_values[s] = []
            #print time_marks
            #print time_values
            #values = time_values[s]
            time_values[s].append(us + "," + v)
            # print "From Smac(" + ethdata[0] + ") Sip(" + ipdata[0] + ":" + str(portdata[0]) + ") to Dmac(" + ethdata[1] + ") Dip(" + ipdata[1] + ":" + str(portdata[1]) + ")"
            # print str(len(data)) + " data:" + str(value)
            count += 1
        #if count == 1:
        #    start = datetime.datetime.now()
        #elif count >= 44000:
        #    print datetime.datetime.now() - start
        #    break

    else:
        continue


#stopDataGeneration()

print total_values.keys()

for key in total_values.keys():
    for k in time_values.keys():
        time_values = total_values[key]
        values = time_values[k]
        print key + ", " + k +": " + str(len(values))




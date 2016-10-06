# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import argparse
import signal
import threading
import time
import sys

import data_generator
import data_transmitter
import sync_client


'''
##########################################################################
### Arguments
##########################################################################
'''

parser = argparse.ArgumentParser(prog='EnergyConsumptionDataTransfer Client', description="This program (%(prog)s)")
parser.add_argument('-V', '--version', action='version', version='%(prog)s version 1.0, Chenfeng Zhu (zhuchenf@gmail.com)')
parser.add_argument('-d', '--datagen', action="store", metavar='True|False', choices=['True', 'False'],
                    help='with data generation or not', dest='datagen', default="False")
parser.add_argument('-f', '--datagenfreq', action="store", metavar='frequency(int)', type=int,
                    help='the frequency of the data generation', dest='datagenfreq', default=44000)
parser.add_argument('-s', '--datastop', action="store", metavar='time(seconds)', type=int,
                    help='stop data generation or not', dest='datastop', default=0)

args = parser.parse_args()


'''
##########################################################################
### Handling
##########################################################################
'''

''' handle with CTRL+C '''
def sigint_handler(signum, frame):
    # write data into file
    # flushData()
    data_transmitter.stopTransmission()
    sync_client.stopSync()
    try:
        data_generator.stopDataGeneration()
    except:
        print "Generation has already stopped."
    print 'Stop pressing the CTRL+C!'
    sys.exit(1)

signal.signal(signal.SIGINT, sigint_handler)


'''
##########################################################################
### Data Generation
##########################################################################
'''


if args.datagen is not None and args.datagen == "True":
    if args.datagenfreq is not None:
        data_generator.startDataGeneration(0, args.datagenfreq)
    else:
        data_generator.startDataGeneration(0)

if args.datastop is not None:
    if args.datastop >= 0:
        print "Client Main: Data generation will stop in " + str(args.datastop) + " seconds."
        time.sleep(args.datastop)
        threading.Thread(target=data_generator.stopDataGeneration, args=[]).start()
    else:
        print "Client Main: Data generation will NOT stop automatically."
else:
    threading.Thread(target=data_generator.stopDataGeneration, args=[]).start()


'''
##########################################################################
### Data Transmit
##########################################################################
'''

print "Client Main: Start transmitting data..."
transmit_thread = threading.Thread(target=data_transmitter.transmitData, args=[])
#transmit_thread.daemon = True
transmit_thread.start()

'''
##########################################################################
### Data Sync
##########################################################################
'''

print "Client Main: Start data synchronization..."
sync_thread = threading.Thread(target = sync_client.startSync, args = [])
#sync_thread.daemon = True
sync_thread.start()


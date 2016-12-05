# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import argparse
import signal
import sys
import threading
import time

import data_receiver
import sync_server

'''
##########################################################################
### Arguments
##########################################################################
'''

parser = argparse.ArgumentParser(prog='EnergyConsumptionDataTransfer Server', description="This program (%(prog)s)")
parser.add_argument('-V', '--version', action='version', version='%(prog)s version 1.0, Chenfeng Zhu (zhuchenf@gmail.com)')
    
args = parser.parse_args()

'''
##########################################################################
### Handling
##########################################################################
'''

def logStr():
    return "Server Main [" + time.strftime("%Y-%m-%d %H:%M:%S") + "]: " # __file__

''' handle with CTRL+C '''
def sigint_handler(signum, frame):
    print logStr(), "Stop pressing the CTRL+C!"
    sys.exit(1)

signal.signal(signal.SIGINT, sigint_handler)


'''
##########################################################################
### Data Receive
##########################################################################
'''

data_receiver.startListening()
print logStr(), "Start receiving data..."
receive_thread = threading.Thread(target=data_receiver.receiveData, args=[])
#receive_thread.daemon = True
receive_thread.start()

'''
##########################################################################
### Data Sync Server
##########################################################################
'''

print logStr(), "Start data synchronization..."
sync_thread = threading.Thread(target = sync_server.startSync, args = [])
sync_thread.start()



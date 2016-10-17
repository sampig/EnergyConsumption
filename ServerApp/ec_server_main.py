# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import argparse
import signal
import sys
import threading

import data_receiver
#import sync_server

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

''' handle with CTRL+C '''
def sigint_handler(signum, frame):
    print 'Stop pressing the CTRL+C!'
    sys.exit(1)

signal.signal(signal.SIGINT, sigint_handler)


'''
##########################################################################
### Data Receive
##########################################################################
'''

data_receiver.startListening()
print "Server Main: Start receiving data..."
receive_thread = threading.Thread(target=data_receiver.receiveData, args=[])
#receive_thread.daemon = True
receive_thread.start()

'''
##########################################################################
### Data Sync Server
##########################################################################
'''

print "Server Main: Start data synchronization..."
#sync_thread = threading.Thread(target = sync_server.startSync, args = [])
#sync_thread.start()



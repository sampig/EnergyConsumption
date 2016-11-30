# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import time
import threading
import sys
import pjsua
import datetime

from ecserver.conf import properties_reader
from ecserver.utils import sip_config, cassandra_util

LOG_LEVEL = 3

SLEEP_TIME = 5

PUBLIC_ADDR = properties_reader.getSIPPublicAddr()
PORT = properties_reader.getSIPPort()

# pending_pres = None
# pending_uri = None

pjsua_lib = None
pjsua_transport = None

buddy_list = []

db_connection = None
db_manager = None

values = []

class ClientCallback(sip_config.MyBuddyCallback):

    def on_pager(self, mime_type, body):
        # print "Instant message from", self.buddy.info().uri,
        # print "(", mime_type, "):"
        # print body
        self.handleData(body)
        # values.append(body)

    def handleData(self, body):
        global db_manager
        first = body.index("|")
        second = body.index("|", first + 1)
        third = body.index("||", second + 1)
        dev_id = body[0:first]
        time_str = datetime.datetime.fromtimestamp(float(body[(first + 1):second])).strftime("%Y%m%d%H%M%S")
        data_str = body[(second + 1):third]
        # arr = body.split("|")
        # data_arr = arr[2].split(";")
        # count = len(data_arr)
        # startus = arr[2]
        # endus = arr[3]
        # count = int(arr[4])
        # data_str = arr[2]
        # print "receive: " + dev_id + "|" + time_str  # + "|" + data_str # + ":" + str(count)
        db_manager.insertConsumptionRaw(dev_id, time_str, data_str, ";")
        return body

def serverListening():

    global pjsua_lib, pjsua_transport  # , pending_pres, pending_uri

    pjsua_lib = pjsua.Lib()

    try:
        # my_ua_cfg = pj.UAConfig()
        # my_ua_cfg.stun_host = PUBLIC_ADDR + ":" + str(PORT)
        pjsua_lib.init(log_cfg=pjsua.LogConfig(level=LOG_LEVEL))
        # lib.init(ua_cfg = my_ua_cfg)

        # Create UDP transport which listens to any available port
        transportConfig = pjsua.TransportConfig(port=PORT, bound_addr='', public_addr=PUBLIC_ADDR)
        pjsua_transport = pjsua_lib.create_transport(pjsua.TransportType.UDP, transportConfig)

        print "\nListening on", pjsua_transport.info().host, ":", pjsua_transport.info().port, "\n"

        # Start the library
        pjsua_lib.start()

        acc = pjsua_lib.create_account_for_transport(pjsua_transport, cb=sip_config.MyAccountCallback())
        acc.buddy_list = buddy_list
        acc.buddy_uri_list = []
        acc.set_basic_status(True)

        my_sip_uri = "sip:" + pjsua_transport.info().host + ":" + str(pjsua_transport.info().port)

        print "\nPublic URI: " + my_sip_uri + "\n"

        # start = datetime.datetime.now()

        while True:
            buddy = None
            if sip_config.pending_pres:
                acc.pres_notify(sip_config.pending_pres, pjsua.SubscriptionState.ACTIVE)
                buddy = acc.add_buddy(sip_config.pending_uri, cb=ClientCallback())
                buddy.subscribe()
                buddy_list.append(buddy)
                acc.buddy_uri_list.append(sip_config.pending_uri)
                # start = datetime.datetime.now()
                print "a client connected: " + sip_config.pending_uri
                print "new buddy list (" + str(len(acc.buddy_uri_list)) + "):",
                print acc.buddy_uri_list
                sip_config.pending_pres = None
                sip_config.pending_uri = None
            elif len(buddy_list) < 1:
                time.sleep(SLEEP_TIME)
                print "No clients. Continue listening..."
            # else:
            #    if len(values) == 1:
            #        start = datetime.datetime.now()
            #    elif len(values) == 440:
            #        end = datetime.datetime.now()
            #        print end - start
                    # break

    except pjsua.Error, e:
        print "Exception: " + str(e)
        pjsua_lib.destroy()
        pjsua_lib = None
    except:
        print "Unexpected error:", sys.exc_info()[0]

#
def stop():
    global pjsua_lib, pjsua_transport
    try:
        # Shutdown the library
        pjsua_transport = None
        pjsua_lib.destroy()
        pjsua_lib = None
    except pjsua.Error, e:
        print "Exception: " + str(e)

#
def startListening():
    global db_connection, db_manager
    db_connection = cassandra_util.CassandraConnection()
    db_manager = cassandra_util.CassandraManager(db_connection)
    threading.Thread(target=serverListening, args=[]).start()

#
def receiveData():

    while True:
        # time.sleep(SLEEP_TIME)
        # print "current buddy list:", str(len(buddy_list))
        pass

# 
def calculateChecksum():
    pass

if __name__ == "__main__":
    startListening()
    receiveData()


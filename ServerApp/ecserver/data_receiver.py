# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import sys
import time
import datetime
import pjsua as pj

from conf import properties_reader


LOG_LEVEL = 3
pending_pres = None
pending_uri = None

PUBLIC_ADDR = properties_reader.getSIPPublicAddr()
PORT = properties_reader.getSIPPort()

SLEEP_TIME = 5

values = []

class MyAccountCallback(pj.AccountCallback):
    def __init__(self, account=None):
        pj.AccountCallback.__init__(self, account)

    def on_incoming_subscribe(self, buddy, from_uri, contact_uri, pres):
        global pending_pres, pending_uri
        # Allow buddy to subscribe to our presence
        if buddy:
            return (200, None)
        pending_pres = pres
        pending_uri = from_uri
        return (202, None)

class MyBuddyCallback(pj.BuddyCallback):
    def __init__(self, buddy=None):
        pj.BuddyCallback.__init__(self, buddy)

    def on_state(self):
        #print "Buddy", self.buddy.info().uri, "is",
        #print self.buddy.info().online_text
        pass

    def on_pager(self, mime_type, body):
        #print "Instant message from", self.buddy.info().uri, 
        #print "(", mime_type, "):"
        #print body
        values.append(body)

    def on_pager_status(self, body, im_id, code, reason):
        if code >= 300:
            print "Message delivery failed for message",
            print body, "to", self.buddy.info().uri, ":", reason

    def on_typing(self, is_typing):
        pass
        #if is_typing:
        #    print self.buddy.info().uri, "is typing"
        #else:
        #    print self.buddy.info().uri, "stops typing"



lib = pj.Lib()

try:
    #my_ua_cfg = pj.UAConfig()
    #my_ua_cfg.stun_host = PUBLIC_ADDR + ":" + str(PORT)
    lib.init(log_cfg = pj.LogConfig(level=LOG_LEVEL))
    #lib.init(ua_cfg = my_ua_cfg)
    
    # Create UDP transport which listens to any available port
    transportConfig = pj.TransportConfig(port=PORT, bound_addr='', public_addr=PUBLIC_ADDR)
    transport = lib.create_transport(pj.TransportType.UDP, transportConfig)
    
    print "\nListening on", transport.info().host, ":", transport.info().port, "\n"

    # Start the library
    lib.start()
    
    acc = lib.create_account_for_transport(transport, cb=MyAccountCallback())
    acc.set_basic_status(True)
    
    my_sip_uri = "sip:" + transport.info().host + ":" + str(transport.info().port)
    
    print "Public URI: " + my_sip_uri
    
    buddy = None

    start = datetime.datetime.now()

    while True:
        if not buddy:
            if pending_pres:
                acc.pres_notify(pending_pres, pj.SubscriptionState.ACTIVE)
                buddy = acc.add_buddy(pending_uri, cb=MyBuddyCallback())
                buddy.subscribe()
                start = datetime.datetime.now()
                print "a client connected: " + pending_uri
                pending_pres = None
                pending_uri = None
            else:
                time.sleep(SLEEP_TIME)
                print "No clients. Continue listening..."
        else:
            if len(values) == 1:
                start = datetime.datetime.now()
                print start
            elif len(values) >= 440:
                end = datetime.datetime.now()
                print end - start
                break


    # Shutdown the library
    transport = None
    lib.destroy()
    lib = None

except pj.Error, e:
    print "Exception: " + str(e)
    lib.destroy()
    lib = None


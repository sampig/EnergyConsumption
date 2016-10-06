# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import pjsua

SLEEP_TIME = 5

pending_pres = None
pending_uri = None

class MyAccountCallback(pjsua.AccountCallback):
    buddy_list = []
    buddy_uri_list = []
    
    def __init__(self, account=None):
        pjsua.AccountCallback.__init__(self, account)

    def on_incoming_subscribe(self, buddy, from_uri, contact_uri, pres):
        global pending_pres, pending_uri
        # Allow buddy to subscribe to our presence
        #print "a client connected: " + from_uri
        # to-do
        if buddy and from_uri in self.buddy_uri_list: #(buddy in self.buddy_list or ):
            return (200, None)
        pending_pres = pres
        pending_uri = from_uri
        return (202, None)

class MyBuddyCallback(pjsua.BuddyCallback):
    def __init__(self, buddy=None):
        pjsua.BuddyCallback.__init__(self, buddy)

    def on_state(self):
        #print "Buddy", self.buddy.info().uri, "is",
        #print self.buddy.info().online_text
        pass

    def on_pager(self, mime_type, body):
        #print "Instant message from", self.buddy.info().uri, 
        #print "(", mime_type, "):"
        #print body
        #self.handleData(body)
        #values.append(body)
        pass

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



# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import pjsua
import time

from ecclient.conf import properties_reader

pending_pres = None
pending_uri = None

pjsua_lib = None
pjsua_transport = None

def logStr():
    return "SIP Config [" + time.strftime("%Y-%m-%d %H:%M:%S") + "]: "

class ECAccountCallback(pjsua.AccountCallback):
    def __init__(self, account=None):
        pjsua.AccountCallback.__init__(self, account)

    def on_incoming_subscribe(self, buddy, from_uri, contact_uri, pres):
        global pending_pres, pending_uri
        # Allow buddy to subscribe to the presence
        if buddy:
            return (200, None)
        pending_pres = pres
        pending_uri = from_uri
        return (202, None)

class MyBuddyCallback(pjsua.BuddyCallback):
    # global is_server_online
    def __init__(self, buddy=None):
        pjsua.BuddyCallback.__init__(self, buddy)

    def on_state(self):
        self.buddy.subscribe()
        # print time.time(), ":", self.buddy.info().uri, "is", self.buddy.info().online_text
        if (self.buddy.info().online_status == 1):  # online
            pass
        else:
            self.buddy.subscribe()
            # print "Reconnect... "

    def on_pager(self, mime_type, body):
        print "Instant message from", self.buddy.info().uri,
        print "(", mime_type, "):"
        # print body

    def on_pager_status(self, body, im_id, code, reason):
        if code >= 300:
            # print "Message delivery failed for message",
            # print body, "to", self.buddy.info().uri, ":", reason
            pass

    def on_typing(self, is_typing):
        if is_typing:
            print self.buddy.info().uri, "is transferring"
        else:
            print self.buddy.info().uri, "stops transferring"

    def test(self):
        print logStr(), "online_status:", self.buddy.info().online_status

def logCB(level, err_str, err_len):
    print logStr(), "(" , str(err_len), ")", err_str

def connect():
    global pjsua_lib, pjsua_transport

    LOG_LEVEL = 1

    HOSTNAME = '127.0.0.1'
    PORT = 23333
    server_uri = properties_reader.getSIPServerURI()  # 'sip:127.0.0.1:34567'

    pjsua_lib = pjsua.Lib()

    try:
        my_ua_cfg = pjsua.UAConfig()
        my_ua_cfg.stun_host = HOSTNAME + ":" + str(PORT)
        pjsua_lib.init(log_cfg=pjsua.LogConfig(level=LOG_LEVEL, callback=logCB))
        # pjsua_lib.init(ua_cfg = my_ua_cfg)

        # Create UDP pjsua_transport which listens to any available port
        transportConfig = pjsua.TransportConfig(port=PORT, bound_addr='', public_addr=HOSTNAME)
        pjsua_transport = pjsua_lib.create_transport(pjsua.TransportType.UDP, transportConfig)

        print logStr(), "Listening on", pjsua_transport.info().host, ":", pjsua_transport.info().port, "\n"

        # Start the library
        pjsua_lib.start()

        acc = pjsua_lib.create_account_for_transport(pjsua_transport, cb=ECAccountCallback())
        acc.set_basic_status(True)

        # my_sip_uri = "sip:" + pjsua_transport.info().host + ":" + str(pjsua_transport.info().port)

        # try to subscribe
        buddy = acc.add_buddy(server_uri, cb=MyBuddyCallback())
        buddy.subscribe()

        print logStr(), "connect to: " + server_uri

        return buddy

    except pjsua.Error, e:
        print logStr()
        print "Exception: " + str(e)
        pjsua_lib.destroy()
        pjsua_lib = None
        return None

def disconnect():
    global pjsua_lib, pjsua_transport
    try:
        # Shutdown the library
        pjsua_transport = None
        pjsua_lib.destroy()
        pjsua_lib = None
    except pjsua.Error, e:
        print "Exception: " + str(e)

if __name__ == "__main__":
    buddy = connect()
    time.sleep(2)
    print "buddy: " + str(buddy)
    if not buddy:
        pass
    else:
        buddy.send_typing_ind(True)
        #
        arr = []
        time_second = str(1476612899)
        for i in xrange(0, 200):
            arr.append("123456," + str(1234.456))
        msg = "aabbccddeeff|" + time_second + "|" + ";".join(arr)
        if msg == "":
            buddy.send_typing_ind(False)
        else:
            buddy.send_pager(msg)
            print "Send " + str(len(arr)) + " to server." + str(len(msg))
        #
        arr = []
        for i in xrange(0, 100):
            arr.append("20160909151515123456," + str(1234.456))
        msg = "aabbccddeeff|" + time_second + "|" + "|;".join(arr)
        if msg == "":
            buddy.send_typing_ind(False)
        else:
            buddy.send_pager(msg)
            print "Send " + str(len(arr)) + " to server." + str(len(msg))
        #
        arr = []
        import struct
        for i in xrange(0, 500):
            arr.append(struct.pack("h", 9999) + struct.pack("f", 1234.456))
        msg = "aabbccddeeff|" + time_second + "|" + ";".join(arr)
        if msg == "":
            buddy.send_typing_ind(False)
        else:
            buddy.send_pager(msg)
            print "Send " + str(len(arr)) + " to server." + str(len(msg))
        #
        arr = []
        for i in xrange(0, 350):
            arr.append(struct.pack("i", 999999) + struct.pack("f", 1234.456))
        d = ";".join(arr)
        msg = "aabbccddeeff|" + time_second + "|" + d
        if msg == "":
            buddy.send_typing_ind(False)
        else:
            buddy.send_pager(msg)
            print "Send " + str(len(arr)) + "|" + str((len(d) + 1) / 9) + " to server." + str(len(msg))
        # time.sleep(1/4400)
    disconnect()


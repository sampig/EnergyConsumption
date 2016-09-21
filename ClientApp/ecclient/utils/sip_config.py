import pjsua as pj

class MyAccountCallback(pj.AccountCallback):
    def __init__(self, account=None):
        pj.AccountCallback.__init__(self, account)

    def on_incoming_subscribe(self, buddy, from_uri, contact_uri, pres):
        global pending_pres, pending_uri
        # Allow buddy to subscribe to our presence
        if buddy:
            return (200, None)
        #print 'Incoming SUBSCRIBE request from', from_uri
        #print 'Press "A" to accept and add, "R" to reject the request'
        pending_pres = pres
        pending_uri = from_uri
        return (202, None)

class MyBuddyCallback(pj.BuddyCallback):
    def __init__(self, buddy=None):
        pj.BuddyCallback.__init__(self, buddy)

    def on_state(self):
        print "Buddy", self.buddy.info().uri, "is", self.buddy.info().online_text

    def on_pager(self, mime_type, body):
        print "Instant message from", self.buddy.info().uri, 
        print "(", mime_type, "):"
        print body

    def on_pager_status(self, body, im_id, code, reason):
        if code >= 300:
            print "Message delivery failed for message",
            print body, "to", self.buddy.info().uri, ":", reason

    def on_typing(self, is_typing):
        if is_typing:
            print self.buddy.info().uri, "is typing"
        else:
            print self.buddy.info().uri, "stops typing"


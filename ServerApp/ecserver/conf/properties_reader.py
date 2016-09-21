import ConfigParser

config = ConfigParser.RawConfigParser()
config.read('config.properties')

def getSIPPublicAddr():
    public_addr = config.get("SIP", "sip.public_addr")
    return public_addr

def getSIPPort():
    port = config.getint("SIP", "sip.port")
    return port

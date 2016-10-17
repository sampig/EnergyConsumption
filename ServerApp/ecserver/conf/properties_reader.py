# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import ConfigParser
import os

config = ConfigParser.RawConfigParser()
configFile = os.path.join(os.path.abspath(os.path.dirname(__file__)), "config.properties")
config.read(configFile)

def getSIPPublicAddr():
    public_addr = config.get("SIP", "sip.public_addr")
    return public_addr

def getSIPPort():
    port = config.getint("SIP", "sip.port")
    return port

def getCassandraHost():
    host = config.get("CassandraDB", "cassandra.host")
    return host

def getCassandraPort():
    port = config.getint("CassandraDB", "cassandra.port")
    return port

def getCassandraKeyspace():
    keyspace = config.get("CassandraDB", "cassandra.keyspace")
    return keyspace

def getSyncHost():
    ip = config.get("Synchronization", "sync.ip")
    port = config.getint("Synchronization", "sync.port")
    return (ip, port)

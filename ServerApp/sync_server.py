# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import datetime
import socket
import time
import sys

from ecserver.utils import cassandra_util
from ecserver.conf import properties_reader

BUFFER_SIZE = 1024

SLEEP_TIME = 5 # 60

host = properties_reader.getSyncHost()

db_connection = None
db_manager = None

def startListen():
    tcp_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    tcp_sock.bind(host)
    tcp_sock.listen(1)
    return tcp_sock

def startSync():
    global db_connection, db_manager
    
    #db_connection = cassandra_util.CassandraConnection()
    #db_manager = cassandra_util.CassandraManager(db_connection)
    
    tcp_sock = startListen()
    
    while True:
        try:
            conn, addr = tcp_sock.accept()
            while True:
                data = conn.recv(BUFFER_SIZE)
                if not data: continue
                print data
                time.sleep(SLEEP_TIME)
                conn.send("Reply: " + data)
                if data == "finish":
                    conn.send("finish")
                    conn.close()
                    break
        except:
            print __file__ + ": Unexpected error:", sys.exc_info()[0]
            print __file__ + ": Sync Server will restart in " + str(SLEEP_TIME) + "seconds..."
            time.sleep(SLEEP_TIME)
            tcp_sock = startListen()
            continue
    tcp_sock.close()

if __name__ == "__main__":
    startSync()

'''
connection = CassandraConnection()
session = connection.getSession()
print connection.getCluset().get_core_connections_per_host(0)#.get_max_connections_per_host(0)
rows = session.execute('select * from ec_consumption LIMIT 10')

for row in rows:
    print row.device_id.strip(' \t\n\r'),"\t| ", row.ec_date.strip() , "\t| ", str(row.ec_consumption_values)
'''
    
'''
import socket
from time import gmtime, strftime

HOST = '127.0.0.1'
TCP_PORT = 7979

BUFFER_SIZE = 64

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((HOST, TCP_PORT))
s.listen(1)
conn, addr = s.accept()
print 'Connection address:', addr

while True:
    data = conn.recv(BUFFER_SIZE)
    if not data: continue
    s.close()
    print "received TCP message: <", str(data), "> at %s" % strftime("%Y-%m-%d %H:%M:%S +0000", gmtime())
    conn.send(data)

conn.close()
'''
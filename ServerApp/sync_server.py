# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

from ecserver.utils.cassandra_util import CassandraConnection

connection = CassandraConnection()
session = connection.getSession()
print connection.getCluset().get_core_connections_per_host(0)#.get_max_connections_per_host(0)
rows = session.execute('select * from ec_consumption LIMIT 10')

for row in rows:
    print row.device_id.strip(' \t\n\r'),"\t| ", row.ec_date.strip() , "\t| ", str(row.ec_consumption_values)

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
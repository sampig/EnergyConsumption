# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

from cassandra.cluster import Cluster

from ecserver.conf import properties_reader
from ecserver.utils import checksum_util

'''
# The CassandraConnection class provides the connection including cluster and session of Cassandra.
# The configuraion of the connection is stored in the 'config.properties'.
'''
class CassandraConnection():

    def __init__(self):
        self.__connect()

    # Connect to the Cassandra server.
    def __connect(self):
        hosts = [properties_reader.getCassandraHost()]
        self.cluster = Cluster(hosts, port=properties_reader.getCassandraPort())
        self.session = self.cluster.connect(properties_reader.getCassandraKeyspace())

    def getCluset(self):
        return self.cluster

    # Get the session of Cassandra connection
    def getSession(self):
        return self.session

'''
# The CassandraManager class provides the manupilation of the tables.
'''
class CassandraManager():

    table_consumption = "ec_consumption"
    table_checksum = "ec_checksum"

    def __init__(self, connection):
        self.connection = connection

    def insertConsumption(self, row_data):
        session = self.connection.getSession()
        insert_stmt = session.prepare("INSERT INTO " + self.table_consumption +
                                      " (device_id, ec_date, ec_time, ec_consumption_values, values_count, values_checksum) VALUES (?, ?, ?, ?, ?, ?)")
        session.execute(insert_stmt, row_data.getRowData())
    
    def getSingleConsumption(self, row_data):
        session = self.connection.getSession()
        select_stmt = session.prepare("SELECT * FROM " + self.table_consumption + " WHERE device_id=? AND ec_date=? AND ec_time=?")
        return session.execute(select_stmt, row_data.getPriKey())

'''
# The DataConsumption class is the model of data
'''
class DataConsumption():
    
    def __init__(self, did, timestamp, values):
        self.device_id = did
        self.ec_date = timestamp[0:4] + "-" + timestamp[4:6] + "-" + timestamp[6:8]
        self.ec_time = timestamp[8:10] + ":" + timestamp[10:12] + ":" + timestamp[12:14]
        if (values is not None):
            self.ec_values = values
            arr_value = values.split("\n")
            self.count = 0
            self.value_checksum = checksum_util.getArrayMD5(values)
    
    def getPriKey(self):
        return [self.device_id, self.ec_date, self.ec_time]
    
    def getRowData(self):
        return [self.device_id, self.ec_date, self.ec_time, self.ec_values, self.count,]

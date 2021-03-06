# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#

import datetime
import struct

from cassandra.cluster import Cluster

from ecserver.conf import properties_reader
from ecserver.utils import checksum_util

def logStr(classname):
    if classname is None:
        return "Cassandra Util [" + datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + "]: "
    else:
        return "Cassandra Util - " + classname + " [" + datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + "]: "

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
        self.cluster = None
        self.session = None
        try:
            self.cluster = Cluster(hosts, port=properties_reader.getCassandraPort())
            self.session = self.cluster.connect(properties_reader.getCassandraKeyspace())
            print logStr(self.__class__.__name__),
            print "Connect to database ", hosts, "Successfully."
        except:
            print logStr(self.__class__.__name__),
            print "Cannot connect to database ", hosts,
            print ". Please check the database and restart the program.",
            print "Otherwise, the data will NOT be stored!"

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
        if session is None:
            print logStr(self.__class__.__name__),
            print "There is NO database connection!"
            return
        # print "inserting dc: ", row_data.device_id, timestamp, row_data.count
        insert_stmt = session.prepare("INSERT INTO " + self.table_consumption +
                                      " (device_id, ec_date, ec_time, start_us, end_us, ec_consumption_values, values_count, values_checksum, insert_time)" +
                                      " VALUES (?, ?, ?, ?, ?, ?, ?, ?, now())")
        session.execute(insert_stmt, row_data.getRowData())

    def insertConsumptionRaw(self, device_id, timestamp, values, delimiter="\n"):  # count=0, startus=0, endus=0,
        session = self.connection.getSession()
        if session is None:
            print logStr(self.__class__.__name__),
            print "There is NO database connection!"
            return
        ec_date = timestamp[0:8]
        ec_time = timestamp[8:14]
        if (values is not None and len(values) >= 8):
            ec_values = values + "\n"
            # arr_value = values.split(delimiter)
            l = len(values)
            values_count = (l) / 9  # len(arr_value)  # int(count) #
            values_checksum = None  # checksum_util.getArrayMD5(values)
            start_us = str(struct.unpack("i", values[0:4])[0])  # "{:06d}".format(
            # ec_values = start_us + "," + "{:.3f}".format(struct.unpack("f", values[4:8])[0])
            end_us = str(struct.unpack("i", values[(l - 8):(l - 4)])[0])
            # if l <= 9:
            #    pass
            # else:
            #    ec_values += ";"
            #    pos = 9
            #    data_size = 9
            #    while True:
            #        if pos >= l - 9:
            #            ec_values += end_us + "," + "{:.3f}".format(struct.unpack("f", values[(l - 4):])[0])
            #            break
            #        ec_values += str(struct.unpack("i", values[pos:pos + 4])[0]) + "," + "{:.3f}".format(struct.unpack("f", values[pos + 4:pos + 8])[0]) + ";"
            #        pos += data_size
        else:
            return
        # print "inserting raw: ", device_id, timestamp, count
        insert_stmt = session.prepare("INSERT INTO " + self.table_consumption +
                                      " (device_id, ec_date, ec_time, start_us, end_us, ec_consumption_values, values_count, values_checksum, insert_time)" +
                                      " VALUES (?, ?, ?, ?, ?, ?, ?, ?, now())")
        session.execute(insert_stmt, [device_id, ec_date, ec_time, start_us, end_us, ec_values, values_count, values_checksum])

    def insertChecksumP(self, device_id, timestamp, values_checksum, values_count):
        session = self.connection.getSession()
        ec_date = timestamp[0:8]
        ec_time = timestamp[8:14]
        insert_stmt = session.prepare("INSERT INTO " + self.table_checksum +
                                      " (device_id, ec_date, ec_time, ec_type, values_count, values_checksum, check_times)" +
                                      " VALUES (?, ?, ?, 'P', ?, ?, 0)")
        session.execute(insert_stmt, [device_id, ec_date, ec_time, values_count, values_checksum])

    def insertChecksumS(self, device_id, timestamp, values_checksum, values_count):
        session = self.connection.getSession()
        ec_date = timestamp[0:8]
        ec_time = timestamp[8:14]
        insert_stmt = session.prepare("INSERT INTO " + self.table_checksum +
                                      " (device_id, ec_date, ec_time, ec_type, values_count, values_checksum, check_times)" +
                                      " VALUES (?, ?, ?, 'S', ?, ?, 0)")
        session.execute(insert_stmt, [device_id, ec_date, ec_time, values_count, values_checksum])

    def getSingleConsumption(self, row_data):
        session = self.connection.getSession()
        select_stmt = session.prepare("SELECT device_id, ec_date, ec_time, start_us, end_us, ec_consumption_values, values_count, values_checksum," +
                                      " unixTimestampOf(insert_time) AS insert_timestamp" +
                                      " FROM " + self.table_consumption +
                                      " WHERE device_id=? AND ec_date=? AND ec_time=? AND start_us=? AND end_us=?")
        return session.execute(select_stmt, row_data.getPriKey())

    def getSingleConsumptionRaw(self, device_id, timestamp, start_us, end_us):
        session = self.connection.getSession()
        ec_date = timestamp[0:8]
        ec_time = timestamp[8:14]
        select_stmt = session.prepare("SELECT device_id, ec_date, ec_time, start_us, end_us, ec_consumption_values, values_count, values_checksum," +
                                      " unixTimestampOf(insert_time) AS insert_timestamp" +
                                      " FROM " + self.table_consumption +
                                      " WHERE device_id=? AND ec_date=? AND ec_time=? AND start_us=? AND end_us=?")
        return session.execute(select_stmt, [device_id, ec_date, ec_time, start_us, end_us])
    
    def getAllConsumptionP(self, device_id, timestamp):
        session = self.connection.getSession()
        ec_date = timestamp[0:8]
        # ec_time = timestamp[8:14]
        select_stmt = session.prepare("SELECT device_id, ec_date, ec_time, start_us, end_us, ec_consumption_values, values_count, values_checksum," +
                                      " unixTimestampOf(insert_time) AS insert_timestamp" +
                                      " FROM " + self.table_consumption +
                                      " WHERE device_id=? AND ec_date=?" + #AND ec_time=?
                                      " ORDER BY ec_date, ec_time, start_us " +
                                      " ALLOW FILTERING")
        return session.execute(select_stmt, [device_id, ec_date]) #, ec_time
        
        
    def getChecksumPeriodValue(self, device_id, time_str):
        session = self.connection.getSession()
        select_stmt = session.prepare("SELECT device_id, ec_date, ec_time, ec_type, values_count, values_checksum" +
                                      " FROM " + self.table_checksum +
                                      " WHERE device_id=? AND ec_date=? AND ec_time=? AND ec_type=?")
        return session.execute(select_stmt, [device_id, time_str[0:8], time_str[8:14], "P"])
    
    def getChecksumSecValue(self, device_id, time_str):
        session = self.connection.getSession()
        select_stmt = session.prepare("SELECT device_id, ec_date, ec_time, ec_type, values_count, values_checksum" +
                                      " FROM " + self.table_checksum +
                                      " WHERE device_id=? AND ec_date=? AND ec_time=? AND ec_type=?")
        return session.execute(select_stmt, [device_id, time_str[0:8], time_str[8:14], "S"])
    
    def deleteConsumption(self, device_id, time_str):
        session = self.connection.getSession()
        ec_date = timestamp[0:8]
        ec_time = timestamp[8:14]
        delete_stmt = session.prepare("DELETE FROM " + self.table_consumption +
                                      " WHERE device_id=? AND ec_date=? AND ec_time=?")
        session.execute(delete_stmt, [device_id, ec_date, ec_time])

'''
# The DataConsumption class is the model of data
'''
class DataConsumption():

    def __init__(self, did, timestamp, values, delimiter="\n"):
        self.device_id = did
        self.ec_date = timestamp[0:8]  # timestamp[0:4] + "-" + timestamp[4:6] + "-" + timestamp[6:8]
        self.ec_time = timestamp[8:14]  # timestamp[8:10] + ":" + timestamp[10:12] + ":" + timestamp[12:14]
        if (values is not None):
            self.ec_values = values
            arr_value = values.split(delimiter)
            self.count = len(arr_value)
            self.value_checksum = checksum_util.getArrayMD5(arr_value)
            self.start_us = arr_value[0][0:6]
            self.end_us = arr_value[len(arr_value) - 1][0:6]

    def getPriKey(self):
        return [self.device_id, self.ec_date, self.ec_time, self.start_us, self.end_us]

    def getRowData(self):
        # (device_id, ec_date, ec_time, start_us, end_us, ec_consumption_values, values_count, values_checksum, insert_time)
        return [self.device_id, self.ec_date, self.ec_time, self.start_us, self.end_us, self.ec_values, self.count, self.value_checksum]

if __name__ == "__main__":
    did = "00aabbccddef"
    timestamp = "20160909150505"
    values = "123456,1234.456;654321,6543.321"
    dc = DataConsumption(did, timestamp, values, ";")
    print "DataConsumption: " + str(dc.getRowData())
    conn = CassandraConnection()
    cm = CassandraManager(conn)
    cm.insertConsumption(dc)
    rows = cm.getSingleConsumption(dc)
    for row in rows:
        print row.device_id, row.ec_date, row.ec_time, row.start_us, row.end_us, row.insert_timestamp,
        print datetime.datetime.fromtimestamp(float(row.insert_timestamp) / 1000.0).strftime('%Y-%m-%d %H:%M:%S.%f')
        print row.ec_consumption_values


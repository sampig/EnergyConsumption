# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#
# This file is used to check the results.
#

import argparse
import datetime

from cassandra.cluster import Cluster

parser = argparse.ArgumentParser(prog='EnergyConsumptionDataTransfer ServerTest', description="This is %(prog)s. It is used to calculate the quantity of data of a device in a given hour.")
parser.add_argument('-i', '--devid', action="store", metavar='device_id',
                    help='the ID of the device', dest='devid')
parser.add_argument('-d', '--datetime', action="store", metavar='time(YYYYMMDDhhmiss)',
                    help='the date time', dest='datetime')

args = parser.parse_args()

device_id = None
datetime_str = None
now = datetime.datetime.now()
last = now - datetime.timedelta(hours=1)

c_hour = 0
count_hour = 0
count_minute = {}
count_second = {}
for x in xrange(0,60):
    count_minute[x] = 0
for x in xrange(0,3600):
    count_second[x] = 0

if args.devid is not None:
    device_id = args.devid

if args.datetime is not None and len(args.datetime) > 10:
    datetime_str = args.datetime
else:
    datetime_str = last.strftime("%Y%m%d%H%M%S")

date_str = datetime_str[0:8]
hour_str = datetime_str[8:10]
minute_str = datetime_str[10:12]

cluster = Cluster(["127.0.0.1"], port=9042)
session = cluster.connect("mykeyspace")
select_stmt = session.prepare("SELECT device_id, ec_date, ec_time, start_us, end_us, ec_consumption_values, values_count, values_checksum," +
                              " unixTimestampOf(insert_time) AS insert_timestamp" + 
                              " FROM ec_consumption"
                              " WHERE device_id=? AND ec_date=? ALLOW FILTERING") # AND ec_time=?
print select_stmt
rows = session.execute(select_stmt, [device_id, date_str])

for row in rows:
    h = row.ec_time[0:2]
    m = int(row.ec_time[2:4])
    c = row.values_count
    if h == hour_str:
        c_hour += 1
        count_hour += c
        count_minute[m] += c
    #if int(h) > int(hour_str):
        #break;


print "Testing\ncheck result:"
print "The device <" + device_id + ">"
print "has received <" + str(count_hour) + ">(" + str(c_hour) + ") data in",
print "<" + date_str + hour_str + ">."
print "The details:\n", count_minute


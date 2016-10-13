# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#
# This file is used to check the results.
#

import argparse
import datetime
import struct

parser = argparse.ArgumentParser(prog='EnergyConsumptionDataTransfer ClientTest', description="This is %(prog)s. It is used to calculate the quantity of data in a given hour in a file.")
parser.add_argument('-f', '--filepath', action="store", metavar='filepath', type=file,
                    help='the path for the data file', dest='filepath')
parser.add_argument('-d', '--datetime', action="store", metavar='time(YYYYMMDDhhmiss)',
                    help='the date time', dest='datetime')

args = parser.parse_args()

DATA_SIZE = 12 #13

file_handler = None
datetime_str = None
now = datetime.datetime.now()
last = now - datetime.timedelta(hours=1)

count_hour = 0
count_minute = {}
count_second = {}
for x in xrange(0,60):
    count_minute[x] = 0
for x in xrange(0,3600):
    count_second[x] = 0

if args.filepath is not None:
    file_handler = args.filepath

if args.datetime is not None and len(args.datetime) > 10:
    datetime_str = args.datetime
else:
    datetime_str = last.strftime("%Y%m%d%H%M%S")

hour_str = datetime_str[0:10]
minute_str = datetime_str[10:12]

'''
for line in file_handler:
    h = line[0:10]
    m = int(line[10:12])
    if h == hour_str:
        count_hour += 1
        count_minute[m] += 1
    if int(h) > int(hour_str):
        break;
'''

while True:
    b = file_handler.read(DATA_SIZE)
    if len(b) < 12:
        break
    if b:
        t = "{:f}".format(struct.unpack("d", b[0:8])[0])
        v = struct.unpack("f", b[8:])[0]
        dt = datetime.datetime.fromtimestamp(struct.unpack("d", b[0:8])[0])
        dt_str = dt.strftime("%Y%m%d%H%M%S")
        h = dt_str[0:10]
        m = int(dt_str[10:12])
        if h == hour_str:
            count_hour += 1
            count_minute[m] += 1
        elif int(h) > int(hour_str):
            break
    else:
        break

print "Testing\ncheck result:"
print "In <" + file_handler.name + "> this file,"
print "there are <" + str(count_hour) + "> data in",
print "<" + hour_str + ">."
print "The details:\n", count_minute

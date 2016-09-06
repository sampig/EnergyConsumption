from utils import devices_reader, checksum_util
import datetime

print devices_reader.getDevices()
print int(devices_reader.getDevices()[0][0],16)

now = datetime.datetime.now()
us = now.strftime("%Y%m%d%H%M%S%f")

print us
print us[0:14]
print us[14:]


print checksum_util.getMD5("testetsetsetsetsetset")
print checksum_util.getArrayMD5(['20150101,123.456','20150102,321.654','20160101,678.456'])

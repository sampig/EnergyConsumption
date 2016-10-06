# Copyright (C) 2016 Chenfeng Zhu
#
# This file is part of the "Energy Consumption" project.
#
# This file is used to get the hash code.
#

import hashlib


# Get the MD5 value of a text string
def getMD5(text):
    return hashlib.md5(text).hexdigest()

# Get the MD5 value of an array
def getArrayMD5(array, delimiter = "\n"):
    text = delimiter.join(array)
    return getMD5(text)

if __name__ == "__main__":
    arr = []
    for i in xrange(0, 44000):
        v = str(20160909151515) + str('%06d'%i) + ":" + str(i)
        arr.append(v)
    print len(arr)
    print arr[0:10]
    print getArrayMD5(arr)
    print getArrayMD5(arr, "|")
    print getArrayMD5(arr, ",")

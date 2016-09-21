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
def getArrayMD5(array):
    text = "\n".join(array)
    return getMD5(text)

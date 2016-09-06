import hashlib



def getMD5(text):
    return hashlib.md5(text).hexdigest()

def getArrayMD5(array):
    text = "\n".join(array)
    return getMD5(text)

print getMD5("testetsetsetsetsetset")
print getArrayMD5(['20150101,123.456','20150102,321.654','20160101,678.456'])

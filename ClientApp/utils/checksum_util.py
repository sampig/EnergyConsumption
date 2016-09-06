import hashlib



def getMD5(text):
    return hashlib.md5(text).hexdigest()

def getArrayMD5(array):
    text = "\n".join(array)
    return getMD5(text)

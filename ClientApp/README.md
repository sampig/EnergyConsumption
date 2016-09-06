Energy Consumption Client
=========================

Author: [ZHU, Chenfeng](http://about.me/zhuchenfeng)

This application is a part of project Energy Consumption.

The client application is used for:

1. reading data of energy consumption
2. sending data to server
3. saving data into local files


## Testing

### Ostinato

``` shell
pip install python-ostinato=0.7

sudo apt-get install ostinato
```

### PJSUA for Python Module

``` shell
svn co http://svn.pjsip.org/repos/pjproject/trunk pjproject
```

pjlib/include/pj/config_site.h

``` C
// Uncomment to get minimum footprint (suitable for 1-2 concurrent calls only)
//#define PJ_CONFIG_MINIMAL_SIZE

// Uncomment to get maximum performance
//#define PJ_CONFIG_MAXIMUM_SPEED

#include <pj/config_site_sample.h>
```

``` shell
export CFLAGS="$CFLAGS -fPIC"
./configure && make dep && make
cd pjsip-apps/src/python
sudo python ./setup.py install
```




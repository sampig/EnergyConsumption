Energy Consumption Client
=========================

Author: [ZHU, Chenfeng](http://about.me/zhuchenfeng)

This application is a part of project Energy Consumption.

The client application is used for:

1. reading data of energy consumption
2. sending data to server
3. saving data into local files
4. synchronizing the data to server

Table of contents
-----------------

  * [Preparation](#preparation)
  * [Structure](#structure)
  * [Design](#design)
  * [Implementation](#implementation)
  	* [Data Files](#data-files)
  	* [Configuration File](#configuration-file)
  * [References](#references)

## Preparation

There are some tools/packages to be installed before running the programs.

### Ostinato

Use Ostinato to generate pseudo packets. It simulate the power devices(e.g. Lumel, Fibaro).

Install Ostinato and the Python library for it.

``` shell
sudo apt-get install ostinato

pip install python-ostinato=0.7
```

### PJSUA for Python Module

``` shell
svn co http://svn.pjsip.org/repos/pjproject/trunk pjproject
```

create a new file _pjlib/include/pj/config_site.h_:

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

## Structure

* ec_client_main.py
	* data_generation.py
	* data_transmitter.py
	* sync_client.py
	* configuration
		* properties_reader.py
	* utilities
		* checksum_util.py
		* devices_reader.py
		* file_util.py
		* sip_config.py

## Design

1. Use Ostinato to generate testing data in a given frequency.
2. Use SIP to transfer the data to server:
	1. send a package of data every millisecond. 
	2. write the data into files every hour.
3. Sync part will synchronize the data to server every hour:
	1. read the newest checksum values from files.
	2. compare them with the server.

## Implementation

### Data Files

* files for data storage:
	* file name: [device_id]_[timestamp_long].csv
	* file content: YYYYMMDDhhmissSSSSSS,float
* files for checksum storage:
	* [device_id]_[timestamp_long]_[type].checksum
	* file content: filename,YYYYMMDDhh,position,count,checksum
					filename,YYYYMMDDhhmiss,position,count,checksum

### Configuration File

Configuration file is in ecclient/conf directory. Its format is:

```
[Category]
[key]=[values]
```

## References

[Ostinato](http://ostinato.org/)
[PJSIP](http://www.pjsip.org/)


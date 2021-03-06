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
  * [Manual](#manual)
  * [References](#references)

## Preparation

There are some tools/packages to be installed before running the programs.

### Ostinato

Use Ostinato to generate pseudo packets. It simulate the power devices(e.g. Lumel, Fibaro).

Install Ostinato and the Python library for it.

``` shell
sudo apt-get install ostinato

pip install python-ostinato
pip install python-ostinato==0.7
```

If the version of ostinato is too old, try to install a new version. Go to [opensuse site](https://software.opensuse.org/download.html?project=home:pstavirs:ostinato&package=ostinato)

``` shell
sudo sh -c "echo 'deb http://download.opensuse.org/repositories/home:/pstavirs:/ostinato/xUbuntu_15.04/ /' > /etc/apt/sources.list.d/ostinato.list"
wget http://download.opensuse.org/repositories/home:pstavirs:ostinato/xUbuntu_15.04/Release.key
sudo apt-key add - < Release.key
sudo apt-get update
sudo apt-get install ostinato
```

### PJSUA for Python Module

``` shell
sudo apt install libasound2-dev

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
		* config.properties
	* utilities
		* checksum_util.py
		* devices_reader.py
		* devices.dat (currently change to use the first line)
		* file_util.py
		* sip_config.py
	* test
		* test.py

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

> [Category]
> [key]=[values]


## Manual

Before starting, change the _config.properties_ files:

> [Transmit]
> transmit.savefreq=30 # save data file every 30 minutes
> transmit.savefilepath=/home/zhuzhu/Documents/project/test # the path for data files
> transmit.savefilefmt=[device_id]_[timestamp_long].csv # file name format for data files
> 
> [Ostinato] # configuration for ostinato
> ostinato.hostname=127.0.0.1
> ostinato.port=7878
> ostinato.txport=2
> ostinato.rxport=2
> 
> [Files]
> file.devices=devices.dat # device information
> 
> [SIP]
> sip.public_addr=127.0.0.1 # SIP client URI: public address
> sip.port=23333 # SIP client URI: port
> sip.serveruri=sip:127.0.0.1:34567 # the SIP Server URI
> 
> [Sync]
> sync.savefilepath=/home/zhuzhu/Documents/project/test/cs # the path for checksum files
> sync.syncfreq=30 # sync every 30 minutes
> sync.savefilefmt=[device_id]_[timestamp_long]_[type].checksum # file name format for checksum files
> sync.serverip=127.0.0.1 # Sync Server: address
> sync.serverport=27979 # Sync Server: port

Start running the client:

``` shell
sudo python ec_client_main.py -d True -f 1 -s -1
usage: 
  -d True|False, --datagen True|False
                        with data generation or not
  -f frequency(int), --datagenfreq frequency(int)
                        the frequency of the data generation
  -s time(seconds), --datastop time(seconds)
                        stop data generation or not (less than 0 means no stop)

nohup sudo python -u ec_client_main.py -d True -f 44000 -s -1 > client.log 2>&1 &
```

Start checking the result of testing:

```shell
python test.py -f [filename] -d [test_datetime(yyyymmddhhmiss)]
```


## References

[Ostinato](http://ostinato.org/)

[PJSIP](http://www.pjsip.org/)


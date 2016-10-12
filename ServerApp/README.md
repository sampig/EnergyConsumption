Energy Consumption Server
=========================

Author: [ZHU, Chenfeng](http://about.me/zhuchenfeng)

This application is a part of project Energy Consumption.

The server application is used for:

1. receiving data from clients.
2. saving data into database.
3. checking/synchronizing data.

Table of contents
-----------------

  * [Preparation](#preparation)
  * [Structure](#structure)
  * [Design](#design)
  * [Implementation](#implementation)
  * [References](#references)

## Preparation

### Cassandra

Create some tables:

``` sql
-- create keyspace
CREATE KEYSPACE mykeyspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1};

-- create table: ec_consumption
CREATE TABLE ec_consumption 
(device_id text, 
ec_date text, ec_time text, start_us text, end_us text, 
ec_consumption_values text, 
values_count int, values_checksum text, 
insert_date text, 
PRIMARY KEY ((device_id), ec_date, ec_time, start_us, end_us)) ;

-- create table: ec_checksum
CREATE TABLE ec_checksum 
(device_id text, 
ec_date text, ec_time text, 
values_count int, values_checksum text, 
check_time timeuuid, check_times int, 
PRIMARY KEY ((device_id), ec_date, ec_time)) ;
```

### Python lib for Cassandra

Install the Python library for Cassandra.

``` shell
pip install cassandra-driver
```

## Structure

* ec_server_main.py
	* data_receiver.py
	* sync_server.py
	* configuration
		* properties_reader.py
	* utilities
		* cassandra_util.py
		* checksum_util.py
		* sip_config.py

## Design

1. Use SIP to receive the data to clients:
	1. write the data into database every millisecond.
2. Sync part will synchronize the data from client every hour:
	1. get the checksum values from database.
	2. compare them with the clients.
	3. insert/update the incorrect data.

## Implementation


## References

[Cassandra](http://cassandra.apache.org/)

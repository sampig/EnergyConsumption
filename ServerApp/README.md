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
  * [Manual](#manual)
  * [References](#references)

## Preparation

### Cassandra

Install Apache Cassandra and create some tables:

``` sql
-- create keyspace
CREATE KEYSPACE mykeyspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1};

-- create table: ec_consumption
CREATE TABLE ec_consumption 
(device_id text, 
ec_date text, ec_time text, start_us text, end_us text, 
ec_consumption_values text, 
values_count int, values_checksum text, 
insert_time timeuuid, 
PRIMARY KEY ((device_id), ec_date, ec_time, start_us, end_us)
) WITH CLUSTERING ORDER BY (ec_date ASC, ec_time ASC, start_us ASC);

-- create table: ec_checksum
CREATE TABLE ec_checksum 
(device_id text, 
ec_date text, ec_time text, 
values_count int, values_checksum text, 
check_time timeuuid, check_times int, 
PRIMARY KEY ((device_id), ec_date, ec_time, ec_type)
) WITH CLUSTERING ORDER BY (ec_date ASC, ec_time ASC);
```

Check the data in the table:

```sql
-- consumption data
SELECT * FROM ec_consumption LIMIT 10;

SELECT device_id,
ec_date, ec_time, start_us, end_us,
ec_consumption_values,
values_count, values_checksum,
unixTimestampOf(insert_time) AS insert_timestamp
FROM ec_consumption
WHERE device_id=? AND ec_date=?
ALLOW FILTERING;

SELECT count(*) FROM ec_checksum LIMIT 10;

-- checksum values
SELECT * FROM ec_checksum LIMIT 10;

```


### Python lib for Cassandra

Install the Python library for Cassandra.

``` shell
pip install cassandra-driver
```

### PJSUA for Python Module

How to install is shown in the Client part.

## Structure

* ec_server_main.py
	* data_receiver.py
	* sync_server.py
	* configuration
		* properties_reader.py
		* config.properties
	* utilities
		* cassandra_util.py
		* checksum_util.py
		* sip_config.py
	* test
		* test.py

## Design

1. Use SIP to receive the data to clients:
	1. write the data into database every millisecond.
2. Sync part will synchronize the data from client 30 minutes:
	1. get the checksum values from database.
	2. compare them with the clients.
	3. insert/update the incorrect data.

## Implementation

Details in the sources.

## Manual

Before starting, change the _config.properties_ files:

> [SIP]
> sip.public_addr=127.0.0.1 # SIP server URI: public address
> sip.port=34567 # SIP server URI: port
> 
> [CassandraDB]
> cassandra.host=127.0.0.1 # Cassandra Server: address
> cassandra.port=9042 # Cassandra Server: port
> cassandra.keyspace=mykeyspace # Cassandra Server: keyspace name
> 
> [Synchronization]
> sync.syncfreq=30 # every 30 minutes for synchronization
> sync.ip=127.0.0.1 # Sync Server: address
> sync.port=27979 # Sync Server: port


Start running the server:

``` shell
python ec_server_main.py

nohup python -u ec_server_main.py > server.log 2>&1 &
```

Start checking the result for receiving:

``` shell
python test.py -i [dev_id] -d [test_datetime(yyyymmddhhmiss)]
``` 


## References

[Cassandra](http://cassandra.apache.org/)

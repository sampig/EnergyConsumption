Energy Consumption Web Server
=========================

Author: [ZHU, Chenfeng](http://about.me/zhuchenfeng)

This application is a part of project Energy Consumption.

The web server is used for:

1. providing web service.
2. providing format of URL.

## Usage

### Request Data

Request URL:

```
[baseURL]/ecws/deviceID/{device id}/{quantity}
```

Response Data:

``` javascript
{
    'DeviceID':'device_id',
    'Data':[
        value[, ...]
    ]
}
```

### Request all devices

Request URL:

```
[baseURL]/ecws/allDevices/{number}
```

### URL Format

Request URL:

```
[baseURL]/ecws/
```

## Design & Implementation

### REST

Jersey + Gson

### Database: Cassandra

Ignore other tables, there is one important table which stored all data of energy consumption identified by device ID and datetime:

``` sql
-- Create keyspace:
CREATE KEYSPACE mykeyspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1};

-- Create table:
CREATE TABLE consumption (
  deviceid text,
  date text,
  time text,
  ...
  power double,
  PRIMARY KEY (deviceid, date, time)
) WITH ...
;
```

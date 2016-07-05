Energy Consumption Web Server
=========================

Author: [ZHU, Chenfeng](http://about.me/zhuchenfeng)

This application is a part of project Energy Consumption.

The web server is used for:

1. providing web service.

## Usage

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

## Design & Implementation

### Structure: REST

REST (Representational State Transfer):

- Usage of HTTP as message interface
   - GET, POST, PUT, DELETE
- Message format is not specified
- Common are HTML, XML, JSON
- Messages are self-contained
- Client and server do NOT share a session

#### JSON Libraries

Multiple libraries are available: JSON.simple, GSON, Jackson, JSONP, Org.JSON, etc.

[Gson](https://github.com/google/gson) is a Java library capable of converting Java objects into their JSON representation and JSON strings to an equivalent Java object without the need for placing Java annotations in your classes. The best things about Gson are:

- Provides simple toJson() and fromJson methods to convert Java objects to JSON and vice-versa
- Alow pre-existing unmodifiable objects to be converted to and from JSON
- It has extensive support of Java Generics
- Allow custom representation for objects
- Support for arbitrarily complex objects

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

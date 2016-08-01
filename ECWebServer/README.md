Energy Consumption Web Server
=========================

Author: [ZHU, Chenfeng](http://about.me/zhuchenfeng)

This application is a part of project Energy Consumption.

The web server is used for:

1. providing web service.
2. providing format of URL.
3. providing QR Code Generation.

Table of contents
-----------------

  * [Developer's Doc](#developer-doc)
    * [Web Server](#web-server)
    * [DB Server](#db-server)
  * [User's Guide](#user-guide)
    * [Request Data](#request-data)
    * [Request all devices](#request-all-devices)
    * [Request URL Format](#request-url-format)
    * [QR Generation](#qr-generation)
  * [References](#references)

## Developer Doc

Details found in [Scanner APP](https://github.com/sampig/EnergyConsumption/tree/scanner-app/ScannerApp).

### Web Server

Switch to the branch of Web Server. Go into the directory _ECWebServer_.

The main codes are in _src_:

_org.zhuzhu.energyconsumption_: all the java files.

You might need to change the configuration for the database connection. In the _CassandraConnection_ class, change:

1. the NODE_IP for cassandra IP address;
2. the NODE_PORT for cassandra port;
3. the KEYSPACE for keyspace if necessary.

If you have installed Maven, use maven to generate WAR file:

``` sh
[GIT_LOCAL_DIRECTORY]/ECWebServer$ mvn install
```

Deploy the server with the WAR file _ECWebServer-1.0.war_ which is generated in the _target_ directory.

If you have Eclipse for JEE, you could also import the project into Eclipse and deploy it with Eclipse.

Import the existing project.

Run Maven Install to generate WAR file.

### DB Server

Run one of the cassandra clients:

``` sh
$ bin/cqlsh [NODE_IP] [CASSANDRA_PORT]
```

Create the keyspace and a table in the database.

``` sql
-- create the keyspace.
CREATE KEYSPACE mykeyspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1};
-- in the keyspace, create a simple table. the columns below are mandatory.
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

Insert the data into the table.

## User Guide

The index page includes some help information:

![](resources/images/00_1Index.png?raw=true)

### Request Data

Request URL:

```
{baseWSURL}/deviceID/{device id}/{quantity}
```

> Example: http://ecserver-sampig.rhcloud.com/ECWebServer/ecws/deviceID/660BF0945365A2A4/30
>   - {baseWSURL}: http://ecserver-sampig.rhcloud.com/ECWebServer/ecws
>   - {device id}: 660BF0945365A2A4
>   - {quantity}: 30

This will respond with the data in JSON format with spaces. Response Data Example:

``` javascript
{
    'DeviceID':'device_id',
    'Data':[
        value[, ...]
    ]
}
```

![](resources/images/01_1RequestData.png?raw=true)

### Request all devices

Request URL:

```
{baseWSURL}/allDevices/{number}
```

This will respond with the information list of {number} devices.

![](resources/images/01_2AllDevices.png?raw=true)

### Request URL Format

Request URL:

```
{baseWSURL}
```

This will respond with the URL format for requesting data.

![](resources/images/01_3URLFormat.png?raw=true)

### QR Code Generation

In fact, most online QR generator could generate QR codes containing text information for requesting data or changing settings. For user's convenience, the server provides interfaces for creating QR code so that users could avoid incorrect format.

Generate QR codes containing only device ID:

![](resources/images/02_1DeviceID.png?raw=true)

Generate QR codes containing only URL:

![](resources/images/02_2URL.png?raw=true)

Generate QR codes containing text in JSON format for request data:

![](resources/images/02_3JSON.png?raw=true)

Generate QR codes containing setting information:

![](resources/images/02_4Setting.png?raw=true)

## References

Source:

- [Web Server Source](https://github.com/sampig/EnergyConsumption/tree/ecwebserver/ECWebServer)

Relative libraries:

- [ZXing](https://github.com/zxing/zxing)
- [Gson](https://github.com/google/gson)
- [Jersey](https://jersey.java.net/)

Demo:

- [Test Server](http://ecserver-sampig.rhcloud.com/ECWebServer)

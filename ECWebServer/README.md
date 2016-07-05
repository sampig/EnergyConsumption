Energy Consumption Web Server
=========================

Author: [ZHU, Chenfeng](http://about.me/zhuchenfeng)

This application is a part of project Energy Consumption.

The web server is used for:

1. providing web service.


Response Data:

``` json
{
    'DeviceID':'device_id',
    'Data':[
        value[, ...]
    ]
}
```


#### Cassandra Design

Ignore other tables, there is one important table which stored all data of energy consumption identified by device ID and datetime:

``` sql
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

Energy Consumption Scanner App
==============================

Author: [ZHU, Chenfeng](http://about.me/zhuchenfeng)

This is the App of Energy Consumption Scanner. This App will display the diagram of appliances' energy consumption via scanning QR code. This file also includes the part for web server.

Table of contents
-----------------

  * [Schedule](#schedule)
  * [Design](#design)
    * [Work Flow](#workflow)
    * [Data Format](#dataformat)
  * [Developer's Doc](#developerdoc)
    * [Source](#source)
    * [Build Scanner APP](#scannerapp)
    * [Build Server](#server)
  * [User's Guide](#userguide)
    * [Data Format](#dataformat)
    * [Installation](#installation)
    * [Using APP](#usingapp)
  * [References](#references)

## Schedule

Here is the schedule for the development of version 1.x:

0. Pre Stage
    1. Week01 (04.29): Proposal.
    2. Week02 (05.02 - 05.08): Schedule & Design.
1. Stage I - APP: QR Code Scanning Activity
    1. Week03 (05.09 - 05.15): Research on QR Code libraries - ZXing library.
    2. Week04&05 (05.16 - 05.29): Research on ZXing app and Develop a simple scanning APP.
2. Stage II - APP: UI Improvement
    1. Week06 (05.30 - 06.05): Research on Graph libraries and JSON libraries.
    2. Week07 (06.06 - 06.12): Graph Generation.
    3. Week08 (06.13 - 06.19): UI Implementation.
3. Stage III - Web Server
    1. Week09 (06.20 - 06.26): Research on Web Service libraries and free PaaS.
    2. Week10 (06.27 - 07.03): Develop web service server and deploy it on Openshift.
4. Stage IV - APP: Settings
    1. Week11 (07.04 - 07.10): Develop the setting function. 
5. Final Stage
    1. Week12 (07.11 - 07.12): Test.
    2. Week12 (07.12): Presentation & Demostration.
    3. Week13-14 (07.13 - 07.28): Beautify codes.

## Design

### WorkFlow

The APP should work as below:

1. Open the APP. And it would be camera preview.
2. Detect whether there are QR codes in the camera preview.
3. Generate a corresponding trace diagram according its recent energy consumption.
4. Overlay the image with the graph where the QR code is.
5. Configure the settings via scanning QR codes.

### DataFormat

#### Data content in QR Code

This ia about valid information contained in QR Code. There are 3 types:

1. Plain Text:
    1. only Device ID (85BD55A4EB0DAD05)
    2. only URL (start with _http_) (http://ecserver-sampig.rhcloud.com/ECWebServer/ecws/deviceID/85BD55A4EB0DAD05/4) (this type is easiest.)
2. JSON Text: 
              ```
              {'deviceID':'device_id','webserver':'url','quantity':int,direct':boolean}
              ```
              (only deviceID is mandatory and other values are optional. this type is most flexible and adapted to different servers.)
3. Setting via QR Code:
              ```
              SETTINGS={'keyname':'value'}
              ```
``` javascript
// Examples 1:
{'deviceID':'7152C2321494759E','webserver':'http://ecserver-sampig.rhcloud.com/ECWebServer/ecws/','direct':false,quantity:8}
// Examples 2:
SETTINGS={'webserver':'http://ecserver-sampig.rhcloud.com/ECWebServer/ecws/'}
SETTINGS={'quantity':60}
```

The first two types are used to display diagram. The Device ID is stored in Database. If the format is incorrect or the Device ID cannot be found in database, there will be no data to display.

The third type is used to change the configurations of APP which is also the only way to change settings. For now, it only supports to change 2 parameters.

#### The Request URL

The format of URL to request data is like:

```
[baseURL]/{deviceID}/{quantity}
```

#### The Response Content

The response content should be in JSON format as below:

``` javascript
{
    'DeviceID':'device_id',
    'Data':[
        value[, ...]
    ]
}
```

## DeveloperDoc

### Source

Get the source of the whole project from Git:

``` sh
git clone git@github.com:sampig/EnergyConsumption.git [GIT_LOCAL_DIRECTORY]
```

There are some branches in this repository. If the master has not merge the branch "scanner-app" or "ecwebserver", you should switch to those branches and get the newest source of corresponding part.

``` sh
[GIT_LOCAL_DIRECTORY]$ git checkout scanner-app
```

In the ScannerApp directory of "scanner-app" branch, it is an Android Studio project.

``` sh
[GIT_LOCAL_DIRECTORY]$ git checkout ecwebserver
```

In the ECWebServer directory of "ecwebserver" branch, it is a Maven and Eclipse project.

### ScannerAPP

#### Deploy and Build Project

Switch to the branch of APP:

The main codes are in _app/src/main_:

1. assets: binary files including web files (js, html) and image files.
2. java: the java files.
  1. _org.zhuzhu.energyconsumption.scanner_: the codes for APP.
  2. _com.google.zxing.client.android_: the codes in ZXing library which needs to be modified.
3. res: xml files for layout or values definition and image files for logo.

Open Android Studio.

![](resources/images/01_1OpenAndroidStudio.png?raw=true)

Open an existing Android Studio project. In the popup dialog, choose _[GIT_LOCAL_DIRECTORY]/ScannerApp_. And choose "use the Gradle wrapper" to config Gradle.

![](resources/images/01_2OpenExistingProject.png?raw=true)

![](resources/images/01_3ConfigGradle.png?raw=true)

![](resources/images/01_4WaitingForConfiguration.png?raw=true)

After waiting for initialization, enter the main layout for project.

![](resources/images/02_1ProjectMain.png?raw=true)

You could also try to rebuild the project (it is not required) to make sure that there are no errors.

![](resources/images/02_2RebuildProject.png?raw=true)

#### Install APP

There are 2 ways to install the APP into your devices (mobile or tablet).

If you have installed adb tools and connect your mobile to your computer, run 'app' immediately and choose a connected device.

![](resources/images/03_1RunDebugApp.png?raw=true)

![](resources/images/03_2SelectTarget.png?raw=true)

After a while, you could find that the APP has been installed in your device.

Another way is to use APK file to install. You could choose build --> build APK to generate an APK.

![](resources/images/03_3BuildAPK.png?raw=true)

![](resources/images/03_4GenerateAPK.png?raw=true)

Then an APK file "_app-debug.apk_" would be generated in _[GIT_LOCAL_DIRECTORY]/ScannerApp/app/build/outputs/apk_. Copy the APK file to your device and install it.

### Server

#### Web Server

Switch to the branch of Web Server. Go into the directory _ECWebServer_.

The main codes are in _src_:

_org.zhuzhu.energyconsumption_: all the java files.

You might need to change the configuration for the database connection. In the _CassandraConnection_ class, change:

1. the NODE_IP for cassandra IP address;
2. the NODE_PORT for cassandra port;
3. the KEYSPACE for keyspace if necessary.

![](resources/images/04_1DBConnectionConfig.png?raw=true)

If you have installed Maven, use maven to generate WAR file:

``` sh
[GIT_LOCAL_DIRECTORY]/ECWebServer$ mvn install
```

Deploy the server with the WAR file _ECWebServer-1.0.war_ which is generated in the _target_ directory.

If you have Eclipse for JEE, you could also import the project into Eclipse and deploy it with Eclipse.

Import the existing project:

![](resources/images/04_2ImportProject.png?raw=true)

![](resources/images/04_3SelectProject.png?raw=true)

Run Maven Install to generate WAR file:

![](resources/images/04_4MavenInstall.png?raw=true)

#### DB Server

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

![](resources/images/04_5Database.png?raw=true)

Insert the data into the table.

## UserGuide

### Installation

Copy the APK into your device. Then install it.

### UsingAPP

Open the APP. It will be a camera preview. Start scanning now.

![](resources/images/05_1APPicon.png?raw=true)

If you scan a QR code which contains the information for settings, it will display the new settings. The window will be in the center of the screen. After long pressing the window or scanning another QR code after 10s, the window will disappear.

![](resources/images/05_2SettingQuantity.png?raw=true)

If you scan a QR code which contains the information for identification of an appliance, it will display the diagram which shows the data received from the server. The window will be where the QR code is detected.

![](resources/images/05_3ScanningData.png?raw=true)

If the Device ID is wrong or the server is broken down, there will be no data to respond. The scanning result is like below. Then you should check the Device ID in the QR code or the server's status.

![](resources/images/05_4NoData.png?raw=true)

If format of data in QR code is incorrect (such as that ID is too long or that the JSON format is incorrect), the scanning result is shown as below. Then you should check the contents in the QR code.

![](resources/images/05_5Incorrect.png?raw=true)

> **Tip:** Volume button could control flash light of camera:
> 
> - Volume UP is for flash ON.
> - Volume DOWN is for flash OFF.

Start your APP now:

1. After generating the QR codes and printing them, stick them to the corresponding appliances or plugins.
2. Collect data of energy consumption for the appliances and store them in the database.
3. Start up the web server and make it connected to database successfully.
4. Open APP and scan the printed QR codes.

## References

Source:

- [Scanner APP Source](https://github.com/sampig/EnergyConsumption/tree/scanner-app/ScannerApp)
- [Web Server Source](https://github.com/sampig/EnergyConsumption/tree/ecwebserver/ECWebServer)

Relative libraries:

- [ZXing](https://github.com/zxing/zxing)
- [Google Charts](https://developers.google.com/chart/)
- [Gson](https://github.com/google/gson)
- [Jersey](https://jersey.java.net/)
- [Apache HttpComponents](https://hc.apache.org/)

Demo:

- [Test Server](http://ecserver-sampig.rhcloud.com/ECWebServer)

Energy Consumption Scanner App
==============================

Author: [ZHU, Chenfeng](http://about.me/zhuchenfeng)

This is the App of Energy Consumption.

Table of contents
-----------------

  * [Schedule](#schedule)
  * [Design](#design)
  * [Manual](#manual)
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

### Work flow

The APP should work as below:

1. Open the APP. And it would be camera preview.
2. Detect whether there are QR codes in the camera preview.
3. Generate a corresponding trace diagram according its recent energy consumption.
4. Overlay the image with the graph where the QR code is.
5. Configure the settings via scanning QR codes.

### Data Format

#### Data content in QR Code

Information contained in QR Code. There are 3 types:

1. Plain Text:
    1. only Device ID (85BD55A4EB0DAD05)
    2. only URL (start with _http_) (http://ecserver-sampig.rhcloud.com/ECWebServer/ecws/deviceID/85BD55A4EB0DAD05/4)
2. JSON Text: 
              ```
              {'deviceID':'device_id','webserver':'url','quantity':int,direct':boolean}
              ```
3. Setting via QR Code:
``` javascript
SETTINGS={'keyname':'value'}
// SETTINGS={'webserver':'http://ecserver-sampig.rhcloud.com/ECWebServer/ecws/'}
// SETTINGS={'quantity':60}
```

#### Data in Request

URL:

```
[baseURL]/{deviceID}/{quantity}
```

#### Data in Response

``` json
{
    'DeviceID':'device_id',
    'Data':[
        value[, ...]
    ]
}
```


## Manual

Get the source of APP:

``` sh
git clone -b scanner-app git@github.com:sampig/EnergyConsumption.git [LOCAL_DIRECTORY]
```

Open Android Studio.
Open an existing Android Studio project
    <LOCAL_DIRECTORY>/ScannerApp
Auto config the gradle.

If you have installed adb tools and connect your mobile, run 'app' immediately and choose a connected device.
Or you could choose build --> build APK. Then an APK file would be generated in _[LOCAL_DIRECTORY]/ScannerApp/app/build/outputs_. Copy the APK file to your mobile and install it.

1. Download the apk file.
2. Install it in the smart phone whose Android version is 4.0 or above.
3. Default Configuration is : _http://ecserver-sampig.rhcloud.com/ECWebServer/ecws/deviceID/_ and _60_. If you want to change it, print

Scanner APP Source: https://github.com/sampig/EnergyConsumption/tree/master/ScannerApp
Web Server Source: https://github.com/sampig/EnergyConsumption/tree/master/ECWebServer

### Server

``` sh
git clone -b ecwebserver git@github.com:sampig/EnergyConsumption.git <LOCAL_DIRECTORY>
```



## References

- [ZXing](https://github.com/zxing/zxing)
- [Google Charts](https://developers.google.com/chart/)
- [Gson](https://github.com/google/gson)
- [Jersey](https://jersey.java.net/)
- [Apache HttpComponents](https://hc.apache.org/)


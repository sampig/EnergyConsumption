package org.zhuzhu.energyconsumption.scanner.utils;

import android.app.Activity;

import org.zhuzhu.energyconsumption.scanner.db.SettingsManager;

import java.util.Random;

public abstract class CommonUtils {

    private static String defaultURL = "http://zhuzhu-sampig.rhcloud.com/ECWebServer/ecws/deviceID";

    public static String getRandomHex(int len) {
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while (sb.length() < len) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, len).toUpperCase();
    }

    public static double getRandomValue() {
        return getRandomValue(0, 10);
    }

    public static double getRandomValue(double min, double max) {
        Random r = new Random();
        return ((int) (10000 * ((r.nextDouble() * (max - min)) + min))) / 10000.0;
    }

    public static String getDefaultURL() {
        return defaultURL;
    }

    public static String getDefaultURL(Activity activity) {
        SettingsManager sm = new SettingsManager(activity);
        String url = sm.getWebServer();
        if (url != null) {
            return url;
        }
        return defaultURL;
    }

}

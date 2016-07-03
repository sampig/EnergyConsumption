/*
 * Energy Consumption ( https://github.com/sampig/EnergyConsumption ) - This file is part of Energy Consumption.
 * Copyright (C) 2016 - Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.utils;

import java.util.Random;

/**
 * This class provides some utilities.
 *
 * @author Chenfeng Zhu
 *
 */
public abstract class CommonUtils {

    /**
     * Default URL
     */
    public final static String DEFAULT_URL = "http://ecserver-sampig.rhcloud.com/ECWebServer/ecws/";

    /**
     * The number of data in 1 second.
     */
    public final static int FREQUENCY = 1;

    /**
     *
     * @param len
     * @return
     */
    public static String getRandomHex(int len) {
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while (sb.length() < len) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, len).toUpperCase();
    }

    /**
     * Get a random double number from 0 to 10.
     *
     * @return
     */
    public static double getRandomValue() {
        return getRandomValue(0, 10);
    }

    /**
     * Get a random double number from min to max.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @return
     */
    public static double getRandomValue(double min, double max) {
        Random r = new Random();
        return ((int) (10000 * ((r.nextDouble() * (max - min)) + min))) / 10000.0;
    }

}

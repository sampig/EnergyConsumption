/**
 * Energy Consumption ( https://github.com/sampig/EnergyConsumption ) - This file is part of Energy Consumption.
 * Copyright (C) 2016 - Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner.model;

import com.google.gson.Gson;

import org.zhuzhu.energyconsumption.scanner.utils.CommonUtils;

/**
 * This is the Settings Model.
 *
 * @author Chenfeng Zhu
 */
public class DeviceModel {

    /**
     * Device ID.
     */
    public String deviceID;
    /**
     * The URL for the web service.
     */
    public String webserver;
    /**
     * <b>TRUE</b> request to the URL directly.<br/>
     * <b>FALSE</b> request with the id.
     */
    public boolean direct = true;

    /**
     * Get a DeviceModel instance according to the json data.
     *
     * @param json
     *            a string in json format
     * @return a DeviceModel instance
     */
    public static DeviceModel getInstance(String json) {
        Gson gson = new Gson();
        DeviceModel dm = gson.fromJson(json, DeviceModel.class);
        return dm;
    }

    @Deprecated
    public static DeviceModel generateDevice() {
        DeviceModel dm = new DeviceModel();
        dm.deviceID = CommonUtils.getRandomHex(16);
        dm.webserver = CommonUtils.getDefaultURL();
        return dm;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}

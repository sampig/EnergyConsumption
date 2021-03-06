/**
 * Energy Consumption ( https://github.com/sampig/EnergyConsumption ) - This file is part of Energy Consumption.
 * Copyright (C) 2016 - Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner.model;

import com.google.gson.Gson;

/**
 * This is the Settings Model.
 *
 * @author Chenfeng Zhu
 */
public class SettingsModel {

    /**
     * The URL for the web service.
     */
    public String webserver = null;
    /**
     * The quantity of data in one hour.
     */
    public Integer quantity = -1;

    /**
     * Get a SettingsModel instance according to the json data.
     *
     * @param json a string in json format
     * @return a SettingsModel instance
     */
    public static SettingsModel getInstance(String json) {
        Gson gson = new Gson();
        SettingsModel sm = gson.fromJson(json, SettingsModel.class);
        return sm;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}

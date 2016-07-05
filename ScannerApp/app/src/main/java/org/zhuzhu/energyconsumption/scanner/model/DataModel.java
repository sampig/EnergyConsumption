/*
 * Energy Consumption ( https://github.com/sampig/EnergyConsumption ) - This file is part of Energy Consumption.
 * Copyright (C) 2016 - Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner.model;

import com.google.gson.Gson;

import org.zhuzhu.energyconsumption.scanner.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is the Data Model.
 *
 * @author Chenfeng Zhu
 */
public class DataModel {

    /**
     * Device ID.
     */
    public String deviceID;
    /**
     * The list of data.
     */
    public List<Double> data;

    /**
     * Get a DataModel instance according to the json data.
     *
     * @param json a string in json format
     * @return a DataModel instance
     */
    public static DataModel getInstance(String json) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(json, DataModel.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Deprecated
    public static DataModel generateInstance(String deviceID) {
        return generateInstance(deviceID, 100, 200);
    }

    @Deprecated
    public static DataModel generateInstance(String deviceID, double start, double end) {
        DataModel dm = new DataModel();
        dm.deviceID = deviceID;
        List<Double> list = new ArrayList<>(0);
        Date date = new Date();
        // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        for (int i = 0; i < CommonUtils.getFrequency() * 60 * 60; i++) { // 10/s * 60s/min * 60min/h * 1h
            // map.put(formatter.format(date), CommonUtils.getRandomValue(start, end));
            list.add(CommonUtils.getRandomValue(start, end));
            date.setTime(date.getTime() - 100);
        }
        dm.data = list;
        return dm;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}

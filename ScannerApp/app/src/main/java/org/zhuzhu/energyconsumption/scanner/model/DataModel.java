package org.zhuzhu.energyconsumption.scanner.model;

import com.google.gson.Gson;

import org.zhuzhu.energyconsumption.scanner.utils.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataModel {

    public String deviceID;
    public Map<String, Double> data;

    public static DataModel getRandomInstance(String deviceID) {
        DataModel dm = new DataModel();
        dm.deviceID = deviceID;
        Map<String, Double> map = new HashMap<>(0);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < 100; i++) {
            map.put(formatter.format(date), CommonUtils.getRandomValue(150, 200));
            date.setTime(date.getTime() - 1000);
        }
        dm.data = map;
        return dm;
    }

    public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
    }

}

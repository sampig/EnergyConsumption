/*
 * Copyright (C) 2016 Chenfeng Zhu
 */
package org.zhuzhu.energyconsumption.scanner.result;

import android.webkit.WebView;

import com.google.zxing.Result;

import java.util.Date;
import java.util.List;

/**
 * @author Chenfeng Zhu
 */
public class ResultModel implements Comparable<ResultModel> {

    private String deviceID;
    private Date date;
    private WebView webView;
    private List<DataValue> dataValueList;
    private Result result;

    public ResultModel() {
        this("NULL");
    }

    public ResultModel(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public WebView getWebView() {
        return webView;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    public List<DataValue> getDataValueList() {
        return dataValueList;
    }

    public void setDataValueList(List<DataValue> dataValueList) {
        this.dataValueList = dataValueList;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public int compareTo(ResultModel other) {
        if (this.getDeviceID().equalsIgnoreCase(other.getDeviceID())) {
            return 0;
        } else if (this.getDate().before(other.getDate())) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object v) {
        boolean flag = false;
        if (v instanceof ResultModel) {
            ResultModel other = (ResultModel) v;
            flag = this.getDeviceID().equalsIgnoreCase(other.getDeviceID());
        }
        return flag;
    }

//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 17 * hash + (this.deviceID != null ? this.deviceID.hashCode() : 0);
//        return hash;
//    }

    @Override
    public String toString() {
        return super.toString() + "(" + deviceID + ")";
    }

    public class DataValue {

        private String timestamp;
        private String value;

        public DataValue(String timestamp, String value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

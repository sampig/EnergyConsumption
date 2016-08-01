/**
 * Energy Consumption ( https://github.com/sampig/EnergyConsumption ) - This file is part of Energy Consumption.
 * Copyright (C) 2016 - Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner.utils;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.zhuzhu.energyconsumption.scanner.ECScannerMainActivity;
import org.zhuzhu.energyconsumption.scanner.db.SettingsManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

/**
 * This class provides basic HTTP request methods to get response.
 *
 * @author Chenfeng Zhu
 */
public class HTTPUtils {

    private SettingsManager settingsManager;

    public HTTPUtils(ECScannerMainActivity activity) {
        settingsManager = new SettingsManager(activity);
    }

    /**
     * Send POST request and get the response.
     *
     * @param url the URL
     * @return the response
     */
    public String getRequestPOST(String url) {
        // TODO: implement the POST method
        return null;
    }

    /**
     * Send GET request and get the response.
     *
     * @param url the URL
     * @return the response
     */
    public String getRequestGET(String url) {
        String result = null;
        AsyncTaskOperation operation = new AsyncTaskOperation();
        operation.execute(url);
        try {
            result = operation.get();
            System.out.println("***HTTPUtils.result: " + result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * This class is used to implement asynchronous task.
     */
    private class AsyncTaskOperation extends AsyncTask<String, Void, String> {
        // TODO: to be improved

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            if (params.length < 1 || params[0] == null || "".equalsIgnoreCase(params[0])) {
                return null;
            }
            String realUrl = params[0];
            return requestGET(realUrl);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @SuppressWarnings("deprecation")
        public String requestGET(String url) {
            String result = null;
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response;
            try {
                response = client.execute(request);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    instream.close();
                    result = sb.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    }
}

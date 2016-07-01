/*
 * Copyright (C) 2016 Chenfeng Zhu
 */
package org.zhuzhu.energyconsumption.scanner.utils;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Chenfeng Zhu
 */
public class HTTPUtils {

    private final static String URL = "http://rest-service.guides.spring.io/greeting";

    public String getRequestPOST() {
        // TODO: implement
        HttpClient httpClient = new DefaultHttpClient();
        return null;
    }

    public String getRequestGET(String str) {
        String result = null;
        AsyncTaskOperation operation = new AsyncTaskOperation();
        operation.execute(str);
        try {
            result = operation.get();
            System.out.println("*****result: "+result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String, Double> parseJsonData(String text) {
        Map<String, Double> data = new HashMap<>(0);
        String result = getRequestGET(text);
        System.out.println("*****result: "+result);
        // TODO: implement
        data.put("14:00", 103.0);
        data.put("15:00", 100.0);
        data.put("16:00", 117.0);
        data.put("17:00", 66.0);
        data.put("18:00", 100.0);
        data.put("19:00", 117.0);
        data.put("20:00", 66.0);
        data.put("21:00", 103.0);
//        try {
//            JSONObject json = new JSONObject(result);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        return data;
    }

    private class AsyncTaskOperation extends AsyncTask<String, Void, String> {
        // TODO: implement

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String realUrl = URL;
            return requestGET(realUrl);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

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
                    String line = null;
                    try {
                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                    } catch (IOException e) {
                    }
                    instream.close();
                    result = sb.toString();
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    }
}

/**
 * Energy Consumption ( https://github.com/sampig/EnergyConsumption ) - This file is part of Energy Consumption.
 * Copyright (C) 2016 - Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner.db;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Chenfeng Zhu
 */
public class SettingsManager {

    private final Activity activity;

    private SQLiteOpenHelper dbHelper = null;
    private SQLiteDatabase db = null;

    private static final String[] COLUMNS = {SettingsDBHelper.KEY_COL, SettingsDBHelper.VALUE_COL};

    public SettingsManager(Activity activity) {
        this.activity = activity;
        dbHelper = new SettingsDBHelper(activity);
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = dbHelper.getReadableDatabase();
        }
    }

    /**
     * Get the web service URL in local database.
     *
     * @return the web service URL
     */
    public String getWebServer() {
        Cursor cursor = null;
        try {
            cursor = db.query(SettingsDBHelper.TABLE_NAME, COLUMNS, SettingsDBHelper.KEY_COL + "=?", new String[]{SettingsDBHelper.WEBSERVER_KEY}, null, null, null);
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(SettingsDBHelper.VALUE_COL));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Update the web server in local database.
     *
     * @param webserver the web service URL
     * @return <b>TRUE</b> if updating successfully
     */
    public boolean updateWebServer(String webserver) {
        Cursor cursor = null;
        try {
            cursor = db.query(SettingsDBHelper.TABLE_NAME, COLUMNS, SettingsDBHelper.KEY_COL + "=?", new String[]{SettingsDBHelper.WEBSERVER_KEY}, null, null, null);
            if (cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(SettingsDBHelper.VALUE_COL, webserver);
                int flag = db.update(SettingsDBHelper.TABLE_NAME, values, SettingsDBHelper.KEY_COL + "=?", new String[]{SettingsDBHelper.WEBSERVER_KEY});
                if (flag > 0) {
                    return true;
                }
            } else {
                ContentValues values = new ContentValues();
                values.put(SettingsDBHelper.KEY_COL, SettingsDBHelper.WEBSERVER_KEY);
                values.put(SettingsDBHelper.VALUE_COL, webserver);
                long flag = db.insert(SettingsDBHelper.TABLE_NAME, SettingsDBHelper.KEY_COL, values);
                if (flag > 0) {
                    return true;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * Get the quantity of data per hour in local database.
     *
     * @return the quantity per hour
     */
    public int getQuantity() {
        Cursor cursor = null;
        try {
            cursor = db.query(SettingsDBHelper.TABLE_NAME, COLUMNS, SettingsDBHelper.KEY_COL + "=?", new String[]{SettingsDBHelper.QUANTITY_KEY}, null, null, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(SettingsDBHelper.VALUE_COL));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    /**
     * Update the quantity of data per hour in local database.
     *
     * @param quantity the quantity per hour
     * @return <b>TRUE</b> if updating successfully
     */
    public boolean updateQuantity(int quantity) {
        Cursor cursor = null;
        try {
            cursor = db.query(SettingsDBHelper.TABLE_NAME, COLUMNS, SettingsDBHelper.KEY_COL + "=?", new String[]{SettingsDBHelper.QUANTITY_KEY}, null, null, null);
            if (cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(SettingsDBHelper.VALUE_COL, quantity);
                int flag = db.update(SettingsDBHelper.TABLE_NAME, values, SettingsDBHelper.KEY_COL + "=?", new String[]{SettingsDBHelper.QUANTITY_KEY});
                if (flag > 0) {
                    return true;
                }
            } else {
                ContentValues values = new ContentValues();
                values.put(SettingsDBHelper.KEY_COL, SettingsDBHelper.QUANTITY_KEY);
                values.put(SettingsDBHelper.VALUE_COL, quantity);
                long flag = db.insert(SettingsDBHelper.TABLE_NAME, SettingsDBHelper.KEY_COL, values);
                if (flag > 0) {
                    return true;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * Close database.
     */
    public void close() {
        if (db != null) {
            db.close();
        }
    }

}

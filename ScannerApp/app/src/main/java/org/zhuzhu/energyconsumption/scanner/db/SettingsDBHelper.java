/**
 * Energy Consumption ( https://github.com/sampig/EnergyConsumption ) - This file is part of Energy Consumption.
 * Copyright (C) 2016 - Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This is to manipulate the table in database which saves some key values.
 *
 * @author Chenfeng Zhu
 */
public class SettingsDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "ec_scanner.db";

    /**
     * Table name
     */
    static final String TABLE_NAME = "ec_settings";
    /**
     * Column - keyname
     */
    static final String KEY_COL = "keyname";
    /**
     * Column - keyvalue
     */
    static final String VALUE_COL = "keyvalue";

    /**
     * Keyname - webserver (the URL for web service)
     */
    static final String WEBSERVER_KEY = "webserver";
    /**
     * Keyname - quantity (the quantity of data in 1 hour)
     */
    static final String QUANTITY_KEY = "quantity";

    /**
     * The default value for URL
     */
    public static final String DEFAULT_URL = "http://ecserver-sampig.rhcloud.com/ECWebServer/ecws/deviceID";
    /**
     * The default value for quantity
     */
    public static final int DEFAULT_QUANTITY = 60;

    // private String update_webserver_sql = "";
    // private String update_quantity_sql = "";

    public SettingsDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public SettingsDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public SettingsDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String create_sql = "CREATE TABLE " + TABLE_NAME + "( " + KEY_COL + " TEXT PRIMARY KEY" + ", " + VALUE_COL + " TEXT);";
        sqLiteDatabase.execSQL(create_sql);
        this.initData(sqLiteDatabase);
    }

    /**
     * Initialize the database: insert the default values into the table.
     *
     * @param sqLiteDatabase the database
     */
    public void initData(SQLiteDatabase sqLiteDatabase) {
        String init_webserver_sql = "INSERT INTO " + TABLE_NAME + "(" + KEY_COL + ", " + VALUE_COL + ") VALUES ('" + WEBSERVER_KEY + "' ,'" + DEFAULT_URL + "');";
        String init_quantity_sql = "INSERT INTO " + TABLE_NAME + "(" + KEY_COL + ", " + VALUE_COL + ") VALUES ('" + QUANTITY_KEY + "' ,'" + DEFAULT_QUANTITY + "');";
        sqLiteDatabase.execSQL(init_webserver_sql);
        sqLiteDatabase.execSQL(init_quantity_sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}

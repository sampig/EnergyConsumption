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
     *
     * @return
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
     *
     * @param webserver
     * @return
     */
    public boolean updateWebServer(String webserver) {
        Cursor cursor = null;
        try {
            cursor = db.query(SettingsDBHelper.TABLE_NAME, COLUMNS, SettingsDBHelper.KEY_COL + "=?", new String[]{SettingsDBHelper.WEBSERVER_KEY}, null, null, null);
            if (cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(SettingsDBHelper.KEY_COL, webserver);
                int flag = db.update(SettingsDBHelper.TABLE_NAME, values, SettingsDBHelper.KEY_COL + "=?", new String[]{SettingsDBHelper.WEBSERVER_KEY});
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
     *
     */
    public void close() {
        if (db != null) {
            db.close();
        }
    }

}

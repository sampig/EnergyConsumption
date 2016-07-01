package org.zhuzhu.energyconsumption.scanner.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Chenfeng Zhu
 */
public class SettingsDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "ec_scanner.db";

    static final String TABLE_NAME = "ec_settings";
    static final String KEY_COL = "keyname";
    static final String VALUE_COL = "keyvalue";

    static final String WEBSERVER_KEY = "webserver";

    private String create_sql = "CREATE TABLE " + TABLE_NAME + "( " + KEY_COL + " TEXT PRIMARY KEY" + ", " + VALUE_COL + " TEXT);";

    private String init_data_sql = "INSERT INTO "+ TABLE_NAME + "(" + KEY_COL + ", " + VALUE_COL + ") VALUES ('webserver' ,'http://zhuzhu-sampig.rhcloud.com/ECWebServer/ecws/deviceID');";

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
        sqLiteDatabase.execSQL(create_sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}

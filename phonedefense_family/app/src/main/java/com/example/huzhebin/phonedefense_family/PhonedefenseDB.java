package com.example.huzhebin.phonedefense_family;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 34494 on 2017/2/3.
 */

public class PhonedefenseDB extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "Phonedefense_family.db";
    public static final String PROTECTGRADE = "protectgrade";
    public PhonedefenseDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + PROTECTGRADE + " (Id integer primary key,grade integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PROTECTGRADE);
        onCreate(db);
    }
}

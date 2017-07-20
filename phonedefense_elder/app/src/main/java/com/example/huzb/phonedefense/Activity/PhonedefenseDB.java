package com.example.huzb.phonedefense.Activity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 34494 on 2017/2/3.
 */

public class PhonedefenseDB extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "Phonedefense.db";
    public static final String FAMILYEMAIL_TABLE = "familyEmail";
    public static final String ISFILTER_TABLE = "isfilter";
    public PhonedefenseDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + FAMILYEMAIL_TABLE + " (Id integer primary key, FamilyEmail text)");
        db.execSQL("create table if not exists " + ISFILTER_TABLE + " (Id integer primary key,isfilter boolean)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FAMILYEMAIL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ISFILTER_TABLE);
        onCreate(db);
    }
}

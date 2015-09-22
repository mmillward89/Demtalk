package com.example.mmillward89.demtalk;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Mmillward89 on 15/08/2015.
 */
public class LinkDatabase extends SQLiteOpenHelper {

    public static final String TABLE_LINKS = "links";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LINK = "link";
    public static final String COLUMN_NAME = "name";

    private static final String DATABASE_NAME = "linksnew.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE = "create table "
            + TABLE_LINKS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_LINK
            + " text not null, " + COLUMN_NAME +
            " text not null" +
            ");";


    public LinkDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    /**
     * Creates the database
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    /**
     * Upgrades database if a new version exists
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w(LinkDatabase.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINKS);
        onCreate(db);
    }
}

package com.example.mapstest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DBHelper extends SQLiteOpenHelper
{
    Context context;

    public DBHelper(Context context)
    {

        super(context, context.getString(R.string.db_name), null, 1);
        this.context = context;
    }

    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table " + context.getString(R.string.db_name) + "("
                + "id integer primary key autoincrement,"
                + "name text,"
                + "run blob" + ");");
    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}
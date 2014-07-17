package com.example.mapstest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class LoadActivity extends Activity
{
    DBHelper dbHelper;
    ListView lvLoad;
    Context context;

    int runsNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);
        getActionBar().hide();

        ArrayList<String> names = getNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.load_list_item, names);
        lvLoad = (ListView) findViewById(R.id.lvLoad);
        lvLoad.setAdapter(adapter);
        setListener();
        context = this;
    }

    protected void setListener()
    {
        lvLoad.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                int runNum = runsNum - position - 1;
                Intent intent = new Intent(context, RunShowActivity.class);
                intent.putExtra("runNum", runNum);
                startActivity(intent);
            }
        });
    }

    protected ArrayList<String> getNames()
    {
        ArrayList<String> list = new ArrayList<String>();

        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[]{"name"};
        Cursor cursor = db.query(getString(R.string.db_name), columns, null, null, null, null, null);

        if (cursor.moveToFirst())
        {
            int nameIndex = cursor.getColumnIndex("name");

            do
            {
                list.add(0, cursor.getString(nameIndex));
                ++runsNum;
            }
            while (cursor.moveToNext());
        }
        else
        {
            TextView tvLoad = (TextView) findViewById(R.id.tvLoad);
            tvLoad.setText(R.string.nothing_saved);
        }

        cursor.close();
        dbHelper.close();
        return list;
    }
}

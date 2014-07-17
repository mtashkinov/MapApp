package com.example.mapstest;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;


public class RunShowActivity extends FragmentActivity
{
    SupportMapFragment mapFragment;
    GoogleMap map;
    Run run;
    int runNum;
    MyLocation prevLocation;
    TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.run_show);
        getActionBar().hide();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map == null)
        {
            finish();
            return;
        }

        Intent intent = getIntent();
        runNum = intent.getIntExtra("runNum", -1);
        run = new Run(this, runNum);

        tvName = (TextView) findViewById(R.id.tvName);
        tvName.setText(getName());

        prevLocation = run.locations.get(0);
        LatLng latLng = new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        map.moveCamera(cameraUpdate);
        drawTrack();

    }

    private String getName()
    {
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[]{"name"};
        Cursor cursor = db.query(getString(R.string.db_name), columns, null, null, null, null, null);

        cursor.moveToFirst();
        for (int i = 0; i < runNum; ++i)
        {
            cursor.moveToNext();
        }
        int nameIndex = cursor.getColumnIndex("name");
        String name = cursor.getString(nameIndex);

        cursor.close();
        dbHelper.close();

        return name;
    }

    public void drawTrack()
    {
        for (MyLocation location : run.locations)
        {
            if (prevLocation != location)
            {
                run.drawSegment(map, prevLocation, location);
                prevLocation = location;
            }
        }
    }
}

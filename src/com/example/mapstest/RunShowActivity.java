package com.example.mapstest;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;

public class RunShowActivity extends FragmentActivity implements View.OnClickListener
{
    final int PADDING = 10;

    SupportMapFragment mapFragment;
    GoogleMap map;
    Run run;
    MyLocation prevLocation;
    TextView tvName;
    Button btDelete;
    Button btToTrack;
    DBHelper dbHelper;
    LatLngBounds cameraArea;
    CameraUpdate cameraUpdate;
    int runNum;

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
        tvName.setOnClickListener(this);

        btDelete = (Button) findViewById(R.id.btDelete);
        btDelete.setOnClickListener(this);

        btToTrack = (Button) findViewById(R.id.btToTrack);
        btToTrack.setOnClickListener(this);

        prevLocation = run.locations.get(0);
        drawTrack();

        cameraArea = run.findCameraArea();
        cameraUpdate = CameraUpdateFactory.newLatLngBounds(cameraArea, PADDING);
        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback()
        {
            @Override
            public void onMapLoaded()
            {
                map.animateCamera(cameraUpdate);
            }
        });
    }

    private String getName()
    {
        dbHelper = new DBHelper(this);
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

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.tvName:
                run.rename(this, runNum);
                tvName.setText(run.getName());
                break;
            case R.id.btDelete:
                Run.delete(this, runNum);
                finish();
                break;
            case R.id.btToTrack:
                map.animateCamera(cameraUpdate);
                break;
            default:
                break;
        }
    }
}

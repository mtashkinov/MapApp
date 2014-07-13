package com.example.mapstest;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.*;

import android.widget.*;
import com.example.mapstest.R;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import android.widget.AdapterView.OnItemSelectedListener;

import java.util.Objects;

public class MyActivity extends FragmentActivity implements LocationListener
{
    final int MAX_DISTANCE_DIFFERENCE = 10;

    final int DIFFERENCE_NONE = 0;
    final int DIFFERENCE_ONE = 1;
    final int DIFFERENCE_TWO = 2;
    final int DIFFERENCE_THREE = 3;
    final int DIFFERENCE_FIVE = 5;
    final int DIFFERENCE_TEN = 10;
    final int DIFFERENCE_FIFTEEN = 15;
    final int DIFFERENCE_TWENTY = 20;

    int distance = 0;
    int delta = 0;
    SupportMapFragment mapFragment;
    GoogleMap map;
    LocationManager locationManager;
    TextView tvDistance;
    Location prevLocation;
    Button btStart;
    UiSettings uiSettings;
    Boolean isRecorded = false;
    String[] data = {"Normal", "Satellite", "Hybrid"};

    final String TAG = "myLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getActionBar().hide();
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvDistance.setGravity(Gravity.CENTER);
        btStart = (Button) findViewById(R.id.btStart);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map == null)
        {
            finish();
            return;
        }

        drawDistance();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        uiSettings = map.getUiSettings();
        uiSettings.setCompassEnabled(false);

        setSpinner();
    }

    private void drawDistance()
    {
        tvDistance.setText("Distance: " + distance + " delta: " + delta);
    }

    private void setSpinner()
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setPrompt("Normal");
        spinner.setGravity(Gravity.CENTER);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                switch (position)
                {
                    case 0:
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        tvDistance.setTextColor(Color.BLACK);
                        ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                        break;
                    case 1:
                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        tvDistance.setTextColor(Color.WHITE);
                        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                        break;
                    case 2:
                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        tvDistance.setTextColor(Color.WHITE);
                        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                        break;
                    default:
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, DIFFERENCE_NONE, 0, "Distance for update = 0");
        menu.add(0, DIFFERENCE_ONE, 1, "Distance for update = 1");
        menu.add(0, DIFFERENCE_TWO, 2, "Distance for update = 2");
        menu.add(0, DIFFERENCE_THREE, 3, "Distance for update = 3");
        menu.add(0, DIFFERENCE_FIVE, 4, "Distance for update = 5");
        menu.add(0, DIFFERENCE_TEN, 5, "Distance for update = 10");
        menu.add(0, DIFFERENCE_FIFTEEN, 6, "Distance for update = 15");
        menu.add(0, DIFFERENCE_TWENTY, 7, "Distance for update = 20");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        locationManager.removeUpdates(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, item.getItemId(), this);
        delta = item.getItemId();
        drawDistance();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location)
    {
        moveCamera(location);
        if (isRecorded && (location.distanceTo(prevLocation) < MAX_DISTANCE_DIFFERENCE + delta))
        {
            setMarker(location);
        }
    }

    protected void moveCamera(Location location)
    {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        map.animateCamera(cameraUpdate);
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    public void setMarker(Location location)
    {
        LatLng prevLatLng = new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude());
        LatLng curLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        PolygonOptions polygonOptions = new PolygonOptions()
                .add(prevLatLng).add(curLatLng)
                .strokeColor(Color.RED).strokeWidth(10).fillColor(Color.RED);
        map.addPolygon(polygonOptions);

        distance += location.distanceTo(prevLocation);
        drawDistance();

        prevLocation = location;
    }

    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btStart:
                if (isRecorded)
                {
                    isRecorded = false;
                    btStart.setText("Start");
                }
                else
                {
                    map.clear();
                    distance = 0;
                    drawDistance();
                    prevLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    isRecorded = true;
                    btStart.setText("Stop");
                }
                break;
            case R.id.tvDistance:
            {
                openOptionsMenu();
            }
            default:
                break;
        }
    }
}
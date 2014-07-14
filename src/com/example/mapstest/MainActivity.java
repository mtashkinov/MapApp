package com.example.mapstest;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends FragmentActivity implements View.OnClickListener
{
    SupportMapFragment mapFragment;
    GoogleMap map;
    UiSettings uiSettings;
    Button btNewRun;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getActionBar().hide();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map == null)
        {
            finish();
            return;
        }

        uiSettings = map.getUiSettings();
        uiSettings.setAllGesturesEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setZoomControlsEnabled(false);
        moveCamera();

        btNewRun = (Button) findViewById(R.id.btNewRun);
        btNewRun.setOnClickListener(this);
    }

    protected void moveCamera()
    {
        LatLng latLng = new LatLng(59.8811181, 29.89100325);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        map.moveCamera(cameraUpdate);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btNewRun:
                Intent intent = new Intent(this, RunActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

    }
}

package com.example.mapstest;

import android.graphics.Color;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.*;
import java.util.ArrayList;

public class Run implements Serializable
{
    ArrayList<MyLocation> locations;
    int distance;

    public Run()
    {
        locations = new ArrayList<MyLocation>();
        distance = 0;
    }

    public void drawSegment(GoogleMap map, MyLocation prevLocation, MyLocation curLocation)
    {
        LatLng prevLatLng = new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude());
        LatLng curLatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());

        PolygonOptions polygonOptions = new PolygonOptions()
                .add(prevLatLng).add(curLatLng)
                .strokeColor(Color.RED).strokeWidth(10).fillColor(Color.RED);
        map.addPolygon(polygonOptions);
    }

    public LatLngBounds findCameraArea()
    {
        double minLatitude = locations.get(0).getLatitude();
        double maxLatitude = locations.get(0).getLatitude();
        double minLongitude = locations.get(0).getLongitude();
        double maxLongitude = locations.get(0).getLongitude();

        for (MyLocation location : locations)
        {
            if (location.getLatitude() > maxLatitude)
            {
                maxLatitude = location.getLatitude();
            }
            if (location.getLongitude() < minLatitude)
            {
                minLatitude = location.getLatitude();
            }
            if (location.getLongitude() > maxLongitude)
            {
                maxLongitude = location.getLongitude();
            }
            if (location.getLongitude() < minLongitude)
            {
                minLongitude = location.getLongitude();
            }
        }

        return new LatLngBounds(new LatLng(minLatitude, minLongitude), new LatLng(maxLatitude, maxLongitude));
    }
}

package com.example.mapstest;

import android.graphics.Color;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;

public class DrawHelper
{
    public static void drawSegment(GoogleMap map, MyLocation prevLocation, MyLocation curLocation)
    {
        LatLng prevLatLng = new LatLng(prevLocation.getLatitude(), prevLocation.getLongitude());
        LatLng curLatLng = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());

        PolygonOptions polygonOptions = new PolygonOptions()
                .add(prevLatLng).add(curLatLng)
                .strokeColor(Color.RED).strokeWidth(10).fillColor(Color.RED);
        map.addPolygon(polygonOptions);
    }

    public static LatLngBounds findCameraArea(Run run)
    {
        MyLocation[] locations = run.getLocations();
        double minLatitude = locations[0].getLatitude();
        double maxLatitude = locations[0].getLatitude();
        double minLongitude = locations[0].getLongitude();
        double maxLongitude = locations[0].getLongitude();

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

    public static void drawTrack(Run run, GoogleMap map)
    {
        MyLocation[] locations = run.getLocations();
        MyLocation prevLocation;

        prevLocation = locations[0];
        for (MyLocation location : locations)
        {
            if (prevLocation != location)
            {
                DrawHelper.drawSegment(map, prevLocation, location);
                prevLocation = location;
            }
        }
    }
}

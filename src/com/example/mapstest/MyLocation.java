package com.example.mapstest;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.io.Serializable;

public class MyLocation implements Serializable
{
    private double latitude;
    private double longitude;
    private long time;
    private float speed;
    private float accuracy;
    private float bearing;

    public MyLocation(Location location)
    {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        time = location.getTime();
        speed = location.getSpeed();
        accuracy = location.getAccuracy();
        bearing = location.getBearing();
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public long getTime()
    {
        return time;
    }

    public float getSpeed()
    {
        return speed;
    }

    public float getAccuracy()
    {
        return accuracy;
    }

    public float getBearing()
    {
        return bearing;
    }

    public Location toLocation(String provider)
    {
        Location location = new Location(provider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setTime(time);
        location.setSpeed(speed);
        location.setAccuracy(accuracy);
        location.setBearing(bearing);

        return location;
    }
}

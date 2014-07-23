package com.example.mapstest;

import android.util.Log;

import java.io.*;
import java.util.ArrayList;

public class Run implements Serializable
{
    private ArrayList<MyLocation> locations;
    private int distance;

    public Run()
    {
        locations = new ArrayList<MyLocation>();
        distance = 0;
    }

    public MyLocation[] getLocations()
    {
        MyLocation[] array = new MyLocation[locations.size()];
        array = locations.toArray(array);
        return array;
    }

    public void add(MyLocation location)
    {
        locations.add(location);
        if (locations.size() > 1)
        {
            MyLocation prevLocation = locations.get(locations.size() - 2);
            distance += location.toLocation().distanceTo(prevLocation.toLocation());
        }
    }

    public int getDistance()
    {
        return distance;
    }

    public boolean isEmpty()
    {
        return locations.isEmpty();
    }
}

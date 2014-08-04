package com.example.mapstest;

import java.io.*;
import java.util.ArrayList;

public class Run implements Serializable
{
    private ArrayList<MyLocation> locations;
    private int distance;
    private int steps;

    public Run()
    {
        locations = new ArrayList<MyLocation>();
        distance = 0;
        steps = 0;
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

    public void incSteps()
    {
        ++steps;
    }

    public int getSteps()
    {
        return steps;
    }

    public boolean isEmpty()
    {
        return locations.isEmpty();
    }
}

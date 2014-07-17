package com.example.mapstest;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
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

    public Run(Context context, int position)
    {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[]{"run"};
        Cursor cursor = db.query(context.getString(R.string.db_name), columns, null, null, null, null, null);

        cursor.moveToFirst();
        for (int i = 0; i < position; ++i)
        {
            cursor.moveToNext();
        }
        int runIndex = cursor.getColumnIndex("run");
        try
        {
            Run run = (Run) deserialize(cursor.getBlob(runIndex));
            locations = run.locations;
            distance = run.distance;
        }
        catch (IOException ex)
        {
            System.out.println("IO Exception");
        }
        catch (ClassNotFoundException ex)
        {
            System.out.println("Class Not Found Exception");
        }

        cursor.close();
        dbHelper.close();

    }

    public static byte[] serialize(Object obj) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
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
}

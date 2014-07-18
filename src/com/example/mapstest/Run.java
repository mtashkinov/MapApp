package com.example.mapstest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
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
    private String name;

    public String getName()
    {
        return name;
    }

    public Run()
    {
        locations = new ArrayList<MyLocation>();
        distance = 0;
    }

    public Run(Context context, int position)
    {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[]{"name", "run"};
        Cursor cursor = db.query(context.getString(R.string.db_name), columns, null, null, null, null, null);

        cursor.moveToFirst();
        for (int i = 0; i < position; ++i)
        {
            cursor.moveToNext();
        }
        int nameIndex = cursor.getColumnIndex("name");
        name = cursor.getString(nameIndex);
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

    public void updName(Activity activity, String name, int runNum)
    {
        ContentValues cv = new ContentValues();
        DBHelper dbHelper = new DBHelper(activity);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        this.name = name;
        cv.put("name", name);
        try
        {
            cv.put("run", Run.serialize(this));
        }
        catch (IOException ex)
        {
            System.out.println("IO Exception");
        }

        db.update(activity.getString(R.string.db_name), cv, "id = ?", new String[] { getID(activity, runNum) });

        dbHelper.close();
        activity.recreate();
    }

    public void rename(final Activity activity,final int runNum)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.rename_dialog, null);
        final EditText etName = (EditText) view.findViewById(R.id.etName);

        dialog.setView(view);
        dialog.setTitle(R.string.rename);

        dialog.setNeutralButton(
                R.string.cancel,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg1)
                    {
                    }
                }
        );

        dialog.setPositiveButton(
                R.string.ok,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg2)
                    {
                        String newName = etName.getText().toString();
                        if (newName.equals(""))
                        {
                            Toast.makeText(activity, activity.getString(R.string.empty_name), Toast.LENGTH_SHORT).show();
                            rename(activity, runNum);
                        }
                        else
                        {
                            updName(activity, newName, runNum);
                        }
                    }
                }
        );
        dialog.show();
    }

    public static void delete(final Activity activity, final int runNum)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.delete_run_title);
        dialog.setMessage(R.string.delete_run);

        dialog.setPositiveButton (
                R.string.yes,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg1)
                    {
                        DBHelper dbHelper = new DBHelper(activity);
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete(activity.getString(R.string.db_name), "id = " + getID(activity, runNum), null);
                        Toast.makeText(activity, activity.getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                        db.close();
                        activity.recreate();
                    }
                }
        );

        dialog.setNegativeButton(
                R.string.no,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg2)
                    {
                    }
                }
        );

        dialog.show();
    }

    private static String getID(Context context, int runNum)
    {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = new String[]{"id"};
        Cursor cursor = db.query(context.getString(R.string.db_name), columns, null, null, null, null, null);

        cursor.moveToFirst();
        for (int i = 0; i < runNum; ++i)
        {
            cursor.moveToNext();
        }
        int idIndex = cursor.getColumnIndex("id");
        String result = Integer.toString(cursor.getInt(idIndex));

        cursor.close();
        return result;
    }

    public static void deleteAll(final Activity activity)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.delete_all_title);
        dialog.setMessage(R.string.delete_all);

        dialog.setPositiveButton (
                R.string.yes,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg1)
                    {
                        DBHelper dbHelper = new DBHelper(activity);
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete(activity.getString(R.string.db_name), null, null);
                        Toast.makeText(activity, activity.getString(R.string.all_deleted), Toast.LENGTH_SHORT).show();
                        db.close();
                        activity.recreate();
                    }
                }
        );

        dialog.setNegativeButton(
                R.string.no,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int arg2)
                    {
                    }
                }
        );

        dialog.show();
    }
}

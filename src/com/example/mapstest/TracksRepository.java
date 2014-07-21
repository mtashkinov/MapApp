package com.example.mapstest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public final class TracksRepository extends Application
{
    private final int DEFAULT_CUR_RUN_NUM = -1;

    static final int RENAMED = 0;
    static final int DELETED = 1;
    static final int DELETED_ALL = 2;
    static final int SAVED = 3;

    private ArrayList<RepositoryChangedListener> listeners;

    private int curRunNum;
    private String curRunID;
    private Run curRun;
    private ArrayList<String> names;
    private DBHelper dbHelper;
    private ContentValues cv;
    private SQLiteDatabase db;
    private Cursor cursor;
    private String[] columns;
    private int curRunIndex;

    @Override
    public void onCreate()
    {
        super.onCreate();
        listeners = new ArrayList<RepositoryChangedListener>();
        names  = new ArrayList<String>();
        curRun = null;
        curRunID = null;
        curRunNum = DEFAULT_CUR_RUN_NUM;

        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();
        columns = new String[]{"name"};
        cursor = db.query(getString(R.string.db_name), columns, null, null, null, null, null);
        int nameIndex = cursor.getColumnIndex("name");

        if (cursor.moveToFirst())
        {
            do
            {
                names.add(0, cursor.getString(nameIndex));
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        dbHelper.close();
    }

    public String[] getNames()
    {
        String[] array = new String[names.size()];
        array = names.toArray(array);
        return array;
    }

    public Run getCurRun()
    {
        return curRun;
    }

    public String getCurRunName()
    {
        return names.get(curRunIndex);
    }

    public void setCurRunIndex(int position)
    {
        curRunIndex = position;
        curRunNum = names.size() - position - 1;
        setCurRun();
    }

    public void addListener(RepositoryChangedListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(RepositoryChangedListener listener)
    {
        listeners.remove(listener);
    }

    protected void fireRepositoryChanged(int action)
    {
        for (RepositoryChangedListener listener : listeners)
        {
            listener.repositoryChanged(action);
        }
    }

    private static byte[] serialize(Object obj) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    private void setCurRun()
    {
        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();
        columns = new String[]{"id", "run"};
        cursor = db.query(getString(R.string.db_name), columns, null, null, null, null, null);

        cursor.moveToFirst();
        for (int i = 0; i < curRunNum; ++i)
        {
            cursor.moveToNext();
        }

        int runIndex = cursor.getColumnIndex("run");
        int idIndex = cursor.getColumnIndex("id");
        try
        {
            curRun = (Run) deserialize(cursor.getBlob(runIndex));
            curRunID = Integer.toString(cursor.getInt(idIndex));
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

    public boolean rename(String newName)
    {
        if (newName != "")
        {
            cv = new ContentValues();
            dbHelper = new DBHelper(this);
            db = dbHelper.getWritableDatabase();

            names.set(curRunIndex, newName);
            fireRepositoryChanged(RENAMED);
            cv.put("name", newName);
            try {
                cv.put("run", serialize(curRun));
            } catch (IOException ex) {
                System.out.println("IO Exception");
            }

            db.update(getString(R.string.db_name), cv, "id = ?", new String[] {curRunID});

            dbHelper.close();
            return true;
        }
        else
        {
            return false;
        }
    }

    public void delete()
    {
        curRun = null;
        curRunNum = DEFAULT_CUR_RUN_NUM;
        names.remove(curRunIndex);
        fireRepositoryChanged(DELETED);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
        db.delete(getString(R.string.db_name), "id = " + curRunID, null);
        curRunID = null;
        db.close();
    }

    public void deleteAll()
    {
        names  = new ArrayList<String>();
        curRun = null;
        curRunID = null;
        curRunNum = DEFAULT_CUR_RUN_NUM;
        fireRepositoryChanged(DELETED_ALL);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
        db.delete(getString(R.string.db_name), null, null);
        db.close();
    }

    public void save(Run run)
    {
        cv = new ContentValues();
        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Calendar calendar = Calendar.getInstance();
        String name = dateFormat.format(calendar.getTime());

        names.add(0, name);
        fireRepositoryChanged(SAVED);

        cv.put("name", name);
        try
        {
            cv.put("run", serialize(run));
        }
        catch (IOException ex)
        {
            System.out.println("IO Exception");
        }
        db.insert(getString(R.string.db_name), null, cv);

        dbHelper.close();
    }
}

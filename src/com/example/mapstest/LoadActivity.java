package com.example.mapstest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class LoadActivity extends Activity
{
    final int MENU_RENAME = 0;
    final int MENU_DELETE = 1;
    final int MENU_DELETE_ALL = 2;

    DBHelper dbHelper;
    ListView lvLoad;
    TextView tvLoad;
    ArrayList<String> names;

    int runsNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);
        getActionBar().hide();

        lvLoad = (ListView) findViewById(R.id.lvLoad);
        registerForContextMenu(lvLoad);

        tvLoad = (TextView) findViewById(R.id.tvLoad);
    }

    private void updList()
    {
        names = getNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.load_list_item, names);
        lvLoad.setAdapter(adapter);
        setListener();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updList();
    }

    protected void setListener()
    {
        lvLoad.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                int runNum = runsNum - position - 1;
                Intent intent = new Intent(LoadActivity.this, RunShowActivity.class);
                intent.putExtra("runNum", runNum);
                startActivity(intent);
            }
        });
    }

    protected ArrayList<String> getNames()
    {
        runsNum = 0;
        ArrayList<String> list = new ArrayList<String>();
        TextView tvLoad = (TextView) findViewById(R.id.tvLoad);;

        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = new String[]{"name"};
        Cursor cursor = db.query(getString(R.string.db_name), columns, null, null, null, null, null);

        if (cursor.moveToFirst())
        {
            int nameIndex = cursor.getColumnIndex("name");
            tvLoad.setText(R.string.saved_runs);

            do
            {
                list.add(0, cursor.getString(nameIndex));
                ++runsNum;
            }
            while (cursor.moveToNext());
        }
        else
        {
            tvLoad.setText(R.string.nothing_saved);
        }

        cursor.close();
        dbHelper.close();
        return list;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)contextMenuInfo;
        menu.setHeaderTitle(names.get(info.position));
        String[] menuItems = getResources().getStringArray(R.array.load_menu);
        for (int i = 0; i < menuItems.length; ++i)
        {
            menu.add(0, i, i, menuItems[i]);
        }
    }

    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int runNum = runsNum - info.position - 1;

        switch (item.getItemId())
        {
            case MENU_RENAME:
                Run run = new Run(this, runNum);
                run.rename(this, runNum);
                break;
            case MENU_DELETE:
                Run.delete(this, runNum);
                break;
            case MENU_DELETE_ALL:
                Run.deleteAll(this);
                break;
            default:
                break;
        }

        return super.onContextItemSelected(item);
    }

}

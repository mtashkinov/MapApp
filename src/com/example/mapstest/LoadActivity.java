package com.example.mapstest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class LoadActivity extends Activity
{
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getActionBar().hide();


    }
}

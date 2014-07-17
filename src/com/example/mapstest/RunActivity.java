package com.example.mapstest;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.Gravity;
import android.view.View;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RunActivity extends FragmentActivity implements LocationListener, View.OnClickListener
{
    final String TAG = "myLogs";

    final double ACCURACY = 0.0005;

    final float START_ZOOM = 18;

    final int MAX_DISTANCE_DIFFERENCE = 10;

    final int DIFFERENCE_NONE = 0;
    final int DIFFERENCE_ONE = 1;
    final int DIFFERENCE_TWO = 2;
    final int DIFFERENCE_THREE = 3;
    final int DIFFERENCE_FIVE = 5;
    final int DIFFERENCE_TEN = 10;
    final int DIFFERENCE_FIFTEEN = 15;
    final int DIFFERENCE_TWENTY = 20;

    final int STATUS_START_PRESSED = 0;
    final int STATUS_BACK_PRESSED = 1;

    int delta = 0;

    Boolean isRecording = false;
    Boolean isFollowing = false;
    Boolean isAnythingPainting = false;
    Boolean isFirstLocationData = true;
    Boolean isSaved = false;
    String[] data = {"Normal", "Satellite", "Hybrid"};

    Run curRun = new Run();

    DBHelper dbHelper;

    SupportMapFragment mapFragment;
    GoogleMap map;
    UiSettings uiSettings;
    LocationManager locationManager;

    TextView tvDistance;
    Button btStart;
    Button btSave;

    MyLocation prevLocation;
    MyLocation curLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.run);
        getActionBar().hide();
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvDistance.setGravity(Gravity.CENTER);
        btStart = (Button) findViewById(R.id.btStart);
        btStart.setOnClickListener(this);
        btSave = (Button) findViewById(R.id.btSave);
        btSave.setOnClickListener(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map == null)
        {
            finish();
            return;
        }

        drawDistance();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        uiSettings = map.getUiSettings();
        uiSettings.setCompassEnabled(false);

        setLocationButtonPosition();
        setListeners();
        setSpinner();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        moveCamera(locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER), START_ZOOM);
        curLocation = new MyLocation(locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER));
        isFirstLocationData = true;
    }

    private void setLocationButtonPosition()
    {
        map.setMyLocationEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        View myLocationParent = ((View) mapFragment.getView().findViewById(1).getParent());

        int positionWidth = myLocationParent.getLayoutParams().width;
        int positionHeight = myLocationParent.getLayoutParams().height;

        FrameLayout.LayoutParams positionParams = new FrameLayout.LayoutParams(
                positionWidth, positionHeight);
        positionParams.setMargins(0, 200, 0, 0);

        myLocationParent.setLayoutParams(positionParams);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    private void drawDistance()
    {
        tvDistance.setText("Distance: " + curRun.distance + " delta: " + delta);
    }

    private void setSpinner()
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setPrompt(getString(R.string.normal));
        spinner.setGravity(Gravity.CENTER);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                switch (position)
                {
                    case 0:
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        tvDistance.setTextColor(Color.BLACK);
                        ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                        break;
                    case 1:
                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        tvDistance.setTextColor(Color.WHITE);
                        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                        break;
                    case 2:
                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        tvDistance.setTextColor(Color.WHITE);
                        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                        break;
                    default:
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });
    }

    private void setListeners()
    {
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener()
        {
            @Override
            public boolean onMyLocationButtonClick()
            {
                if (!isFollowing)
                {
                    moveCamera(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER), START_ZOOM);
                    makeToast(getString(R.string.follow));
                    isFollowing = true;
                }
                return true;
            }
        });

        map.setOnCameraChangeListener(
                new GoogleMap.OnCameraChangeListener()
                {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition)
                    {
                        LatLng latLng = cameraPosition.target;
                        if (isFollowing && ((Math.abs(curLocation.getLongitude() - latLng.longitude) > ACCURACY) || (Math.abs(curLocation.getLatitude() - latLng.latitude) > ACCURACY)))
                        {
                            makeToast(getString(R.string.follow_stopped));
                            isFollowing = false;
                        }
                    }
                }
        );
    }

    private void makeToast(String s)
    {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, DIFFERENCE_NONE, 0, "Distance for update = 0");
        menu.add(0, DIFFERENCE_ONE, 1, "Distance for update = 1");
        menu.add(0, DIFFERENCE_TWO, 2, "Distance for update = 2");
        menu.add(0, DIFFERENCE_THREE, 3, "Distance for update = 3");
        menu.add(0, DIFFERENCE_FIVE, 4, "Distance for update = 5");
        menu.add(0, DIFFERENCE_TEN, 5, "Distance for update = 10");
        menu.add(0, DIFFERENCE_FIFTEEN, 6, "Distance for update = 15");
        menu.add(0, DIFFERENCE_TWENTY, 7, "Distance for update = 20");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        locationManager.removeUpdates(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, item.getItemId(), this);
        delta = item.getItemId();
        drawDistance();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location)
    {
        curLocation = new MyLocation(location);
        if (isFirstLocationData)
        {
            isFirstLocationData = false;
            moveCamera(location, START_ZOOM);
        }
        if (isFollowing)
        {
            moveCamera(location, map.getCameraPosition().zoom);
        }
        if (isRecording && (location.distanceTo(prevLocation.toLocation(location.getProvider())) < MAX_DISTANCE_DIFFERENCE + delta))
        {
            if (curRun.locations.isEmpty())
            {
                curRun.locations.add(prevLocation);
            }
            curRun.locations.add(curLocation);
            isAnythingPainting = true;
            curRun.drawSegment(map, prevLocation, curLocation);
            curRun.distance += location.distanceTo(prevLocation.toLocation(location.getProvider()));
            drawDistance();
            prevLocation = curLocation;
        }
    }

    protected void moveCamera(Location location, float zoom)
    {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        map.animateCamera(cameraUpdate);
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }



    private void start()
    {
        curRun = new Run();
        isAnythingPainting = false;
        map.clear();
        drawDistance();
        prevLocation = new MyLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        isRecording = true;
        btStart.setText("Stop");
        btSave.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed()
    {
        if (isAnythingPainting && !isSaved)
        {
            dialogShow(STATUS_BACK_PRESSED);
        }
        else
        {
            finish();
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btStart:
                if (isRecording)
                {
                    isRecording = false;
                    isSaved = false;
                    btStart.setText("Start");
                    if (isAnythingPainting)
                    {
                        btSave.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    if (isAnythingPainting && !isSaved)
                    {
                        dialogShow(STATUS_START_PRESSED);
                    }
                    else
                    {
                        start();
                    }
                }
                break;
            case R.id.tvDistance:
            {
                openOptionsMenu();
                break;
            }
            case R.id.btSave:
            {
                save();
                break;
            }
            default:
                break;
        }
    }

    /*private String getName()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(RunActivity.this);
        final EditText etName = (EditText) findViewById(R.id.etName);

        dialog.setView(findViewById(R.layout.dialog));
        dialog.setTitle(R.string.save_run);

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
                        result = etName.getText().toString();
                    }
                }
        );
        dialog.show();
        return result;
    }*/

    private void save()
    {
        ContentValues cv = new ContentValues();
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Calendar calendar = Calendar.getInstance();
        String name = dateFormat.format(calendar.getTime());


        cv.put("name", name);
        try
        {
            cv.put("run", Run.serialize(curRun));
        }
        catch (IOException ex)
        {
            System.out.println("IO Exception");
        }
        db.insert(getString(R.string.db_name), null, cv);

        dbHelper.close();

        makeToast(getString(R.string.saved));
        isSaved = true;
        btSave.setVisibility(View.GONE);
    }

    private void dialogShow(final int status)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(RunActivity.this);

        switch (status)
        {
            case STATUS_START_PRESSED:
                dialog.setTitle(R.string.new_run);
                break;
            case STATUS_BACK_PRESSED:
                dialog.setTitle(R.string.exit);
                break;
            default:
                break;
        }

        dialog.setMessage(R.string.save_data);

        dialog.setPositiveButton (
            R.string.yes,
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int arg1)
                {
                    save();
                    switch (status)
                    {
                        case STATUS_START_PRESSED:
                            start();
                            break;
                        case STATUS_BACK_PRESSED:
                            finish();
                            break;
                        default:
                            break;
                    }
                }
            }
        );

        dialog.setNeutralButton(
            R.string.cancel,
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int arg2)
                {
                }
            }
        );

        dialog.setNegativeButton(
            R.string.no,
            new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int arg3)
                {
                    switch (status)
                    {
                        case STATUS_START_PRESSED:
                            start();
                            break;
                        case STATUS_BACK_PRESSED:
                            finish();
                            break;
                        default:
                            break;
                    }
                }
            }
        );
        dialog.show();
    }
}
package com.example.mapstest;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.*;
import android.widget.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.Gravity;
import android.view.View;

public class RunActivity extends FragmentActivity implements LocationListener, View.OnClickListener, SensorEventListener
{
    final double LOC_ACCURACY = 0.0005;
    final double ACC_ACCURACY = 3;
    final double MIN_COS = 0.7;

    final float START_ZOOM = 18;

    final int MAX_DISTANCE_DIFFERENCE = 10;
    final int LOCATION_BUTTON_MARGIN = 200;
    final int UPD_TIME = 3000;

    int delta = 0;
    int sequenceLength;

    double[] curVector;
    double[] maxVector;

    Boolean isRecording = false;
    Boolean isFollowing = false;
    Boolean isFirstLocationData = true;
    Boolean isSaved = false;
    String[] data = {"Normal", "Satellite", "Hybrid"};

    Run curRun = new Run();

    TracksRepository tracksRepository;

    SupportMapFragment mapFragment;
    GoogleMap map;
    UiSettings uiSettings;
    LocationManager locationManager;
    SensorManager sensorManager;
    Sensor sensorLinAccel;
    private PowerManager.WakeLock mWakeLock;

    TextView tvDistance;
    TextView tvSteps;
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

        tracksRepository = (TracksRepository)getApplication();

        tvDistance = (TextView) findViewById(R.id.tvDistance);
        btStart = (Button) findViewById(R.id.btStart);
        btStart.setOnClickListener(this);
        btSave = (Button) findViewById(R.id.btSave);
        btSave.setOnClickListener(this);
        tvSteps = (TextView) findViewById(R.id.tvSteps);
        tvSteps.setText("Steps: 0");

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
        registerForContextMenu(tvDistance);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorLinAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myLogs");

        setLocationButtonPosition();
        setListeners();
        setSpinner();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER) != null)
        {
            moveCamera(locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER), START_ZOOM);
        }
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
        positionParams.setMargins(0, LOCATION_BUTTON_MARGIN, 0, 0);

        myLocationParent.setLayoutParams(positionParams);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        locationManager.removeUpdates(this);
        if (isRecording)
        {
            sensorManager.unregisterListener(this);
            mWakeLock.release();
        }
    }

    private void drawDistance()
    {
        tvDistance.setText("Distance: " + curRun.getDistance() + " delta: " + delta);
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
                        if (isFollowing && ((Math.abs(curLocation.getLongitude() - latLng.longitude) > LOC_ACCURACY) || (Math.abs(curLocation.getLatitude() - latLng.latitude) > LOC_ACCURACY)))
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
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo)
    {
        menu.setHeaderTitle("Distance for update:");
        int[] menuItems = getResources().getIntArray(R.array.distance_menu);
        for (int item : menuItems)
        {
            menu.add(0, item, item, Integer.toString(item));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        locationManager.removeUpdates(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, item.getItemId(), this);
        delta = item.getItemId();
        drawDistance();

        return super.onContextItemSelected(item);
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
        if (isRecording && (location.distanceTo(prevLocation.toLocation()) < MAX_DISTANCE_DIFFERENCE + delta))
        {
            if (!curRun.isEmpty())
            {
                curRun.add(prevLocation);
            }
            curRun.add(curLocation);
            DrawHelper.drawSegment(map, prevLocation, curLocation);
            drawDistance();
        }
        prevLocation = curLocation;
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

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (isRecording)
        {
            switch (event.sensor.getType())
            {
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    curVector = new double[4];
                    curVector[0] = event.values[0];
                    curVector[1] = event.values[1];
                    curVector[2] = event.values[2];
                    curVector[3] = Math.sqrt(curVector[0] * curVector[0] + curVector[1] * curVector[1] + curVector[2] * curVector[2]);

                    if (curVector[3] > ACC_ACCURACY)
                    {
                        double scalar;
                        double cos;
                        boolean isStep = false;

                        scalar = curVector[0] * maxVector[0] + curVector[1] * maxVector[1] + curVector[2] * maxVector[2];
                        cos = scalar / curVector[3] / maxVector[3];

                        if (Math.abs(cos) > MIN_COS)
                        {
                            if (cos < MIN_COS)
                            {
                                ++sequenceLength;
                            }
                            else
                            {

                                if (sequenceLength > 2)
                                {
                                    isStep = true;
                                }
                                sequenceLength = 0;
                            }
                        }

                        if (curVector[3] > maxVector[3])
                        {
                            maxVector = curVector;
                        }

                        if (isStep)
                        {
                            maxVector = curVector;
                            curRun.incSteps();
                        }
                        tvSteps.setText("Steps: " + curRun.getSteps());
                    }
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    private void start()
    {
        curRun = new Run();
        map.clear();
        drawDistance();
        sensorManager.registerListener(this, sensorLinAccel, UPD_TIME);
        sequenceLength = 0;
        prevLocation = new MyLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        isRecording = true;
        mWakeLock.acquire();
        tvSteps.setText("Steps: 0");
        btStart.setText("Stop");
        maxVector = new double[4];
        btSave.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed()
    {
        if (((!curRun.isEmpty()) || (curRun.getSteps() != 0))  && !isSaved)
        {
            Dialogs.showSaveDialog(this, curRun, getString(R.string.exit), new DialogClickListener()
            {
                @Override
                public void onDialogNotCanceled(boolean positiveButtonPressed)
                {
                    if (positiveButtonPressed)
                    {
                        saveActions();
                    }
                    finish();
                }
            });
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
                    mWakeLock.release();
                    sensorManager.unregisterListener(this);
                    if ((!curRun.isEmpty()) || (curRun.getSteps() != 0))
                    {
                        btSave.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    if (((!curRun.isEmpty()) || (curRun.getSteps() != 0))  && !isSaved)
                    {
                        Dialogs.showSaveDialog(this, curRun, getString(R.string.new_run), new DialogClickListener()
                        {
                            @Override
                            public void onDialogNotCanceled(boolean positiveButtonPressed)
                            {
                                if (positiveButtonPressed)
                                {
                                    saveActions();
                                }
                                start();
                            }
                        });
                    }
                    else
                    {
                        start();
                    }
                }
                break;
            case R.id.tvDistance:
            {
                openContextMenu(tvDistance);
                break;
            }
            case R.id.btSave:
            {
                tracksRepository.save(curRun);
                saveActions();
                break;
            }
            default:
                break;
        }
    }

    private void saveActions()
    {
        makeToast(getString(R.string.saved));
        isSaved = true;
        btSave.setVisibility(View.GONE);
    }
}
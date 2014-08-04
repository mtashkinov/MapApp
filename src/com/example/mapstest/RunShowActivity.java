package com.example.mapstest;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;

public class RunShowActivity extends FragmentActivity implements View.OnClickListener, RepositoryChangedListener
{
    final int PADDING = 100;

    SupportMapFragment mapFragment;
    GoogleMap map;
    Run run;
    TextView tvName;
    TextView tvDistance;
    TextView tvSteps;
    Button btDelete;
    Button btToTrack;
    LatLngBounds cameraArea;
    CameraUpdate cameraUpdate;
    TracksRepository tracksRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.run_show);
        getActionBar().hide();

        tracksRepository = (TracksRepository)getApplication();
        tracksRepository.addListener(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map == null)
        {
            finish();
            return;
        }

        tvName = (TextView) findViewById(R.id.tvName);
        tvName.setText(tracksRepository.getCurRunName());
        tvName.setOnClickListener(this);

        btDelete = (Button) findViewById(R.id.btDelete);
        btDelete.setOnClickListener(this);

        run = tracksRepository.getCurRun();
        btToTrack = (Button) findViewById(R.id.btToTrack);
        tvDistance = (TextView) findViewById(R.id.tvDistance);

        if (!run.isEmpty())
        {
            btToTrack.setOnClickListener(this);
            DrawHelper.drawTrack(run, map);
            cameraArea = DrawHelper.findCameraArea(run);
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(cameraArea, PADDING);
            map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback()
            {
                @Override
                public void onMapLoaded()
                {
                    map.animateCamera(cameraUpdate);
                }
            });
            tvDistance.setText("Distance: " + run.getDistance());
        }
        else
        {
            tvDistance.setVisibility(View.GONE);
            btToTrack.setVisibility(View.GONE);
        }

        if (run.getSteps() != 0)
        {
            tvSteps = (TextView) findViewById(R.id.tvSteps);
            tvSteps.setText("Steps: " + run.getSteps());
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        tracksRepository.removeListener(this);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.tvName:
                Dialogs.showRenameDialog(this);
                break;
            case R.id.btDelete:
                Dialogs.showDeleteDialog(this);
                break;
            case R.id.btToTrack:
                map.animateCamera(cameraUpdate);
                break;
            default:
                break;
        }
    }

    @Override
    public void repositoryChanged(int actionCode)
    {
        switch (actionCode)
        {
            case TracksRepository.RENAMED:
                tvName.setText(tracksRepository.getCurRunName());
                break;
            case TracksRepository.DELETED:
                finish();
                break;
            case TracksRepository.DELETED_ALL:
                break;
            default:
                break;
        }
    }
}

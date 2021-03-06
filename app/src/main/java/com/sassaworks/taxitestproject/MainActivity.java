package com.sassaworks.taxitestproject;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sassaworks.taxitestproject.database.AppDatabase;
import com.sassaworks.taxitestproject.database.AppExecutor;
import com.sassaworks.taxitestproject.database.LocationRouteDao;
import com.sassaworks.taxitestproject.service.LocationBackgroundService;

import static com.sassaworks.taxitestproject.service.LocationBackgroundService.ACTION_LOCATION_BROADCAST;
import static com.sassaworks.taxitestproject.service.LocationBackgroundService.LAT_DATA;
import static com.sassaworks.taxitestproject.service.LocationBackgroundService.LON_DATA;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MapFragment.OnFragmentInteractionListener, LocationRouteFragment.OnListFragmentInteractionListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    AppDatabase mDb;
    NavigationView navigationView;

    private boolean mLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDb = AppDatabase.getInstance(this);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        if (savedInstanceState==null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, MapFragment.newInstance("1", "2"),MapFragment.class.getSimpleName())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame,MapFragment.newInstance("1","2"),MapFragment.class.getSimpleName());
                    transaction.commit();


            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame,LocationRouteFragment.newInstance(1),LocationRouteFragment.class.getSimpleName());
                    transaction.commit();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction() {
        getLocationPermission();
    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (!LocationBackgroundService.isServiceRun()) {
                Intent intent = new Intent(this, LocationBackgroundService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            }
            getCurrentLocation();
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        if (!LocationBackgroundService.isServiceRun()) {
            Intent intent = new Intent(this, LocationBackgroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }

        getCurrentLocation();
    }

    private void getCurrentLocation() {
        try {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            Task<Location> locationResult = mFusedLocationClient.getLastLocation();

            locationResult.addOnCompleteListener( new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {

                        Location mLastKnownLocation = task.getResult();
                        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
                        intent.putExtra(LAT_DATA, mLastKnownLocation.getLatitude());
                        intent.putExtra(LON_DATA, mLastKnownLocation.getLongitude());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    }
                }
            });
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    @Override
    public void onListFragmentInteraction(LocationRouteDao.TempLocal item) {
        navigationView.setCheckedItem(R.id.nav_camera);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame,MapFragment.newInstance("route",item.getName())).commit();
    }

    @Override
    public void onDeleteRouteListener(String route) {
        AppExecutor.getInstance().getDbExecutor().execute(()->{
            mDb.routeDao().deleteByName(route);
        });
    }
}

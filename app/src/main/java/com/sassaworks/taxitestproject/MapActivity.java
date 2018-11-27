package com.sassaworks.taxitestproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.sassaworks.taxitestproject.broadcast.TrackReceiver;
import com.sassaworks.taxitestproject.service.LocationBackgroundService;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Marker mCurrentMarker = null;
    private boolean mLocationPermissionGranted;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double latitude = intent.getDoubleExtra(LocationBackgroundService.LAT_DATA,0);
                double longitude = intent.getDoubleExtra(LocationBackgroundService.LON_DATA,0);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(latitude,
                                    longitude),15));
                    if (mCurrentMarker != null) mCurrentMarker.remove();
                    mCurrentMarker = mMap.addMarker(new MarkerOptions()
                            .title("My location")
                            .position(new LatLng(latitude,
                                    longitude)));
            }
        },new IntentFilter(LocationBackgroundService.ACTION_LOCATION_BROADCAST));

//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null) {
//                    return;
//                }
//                for (Location location : locationResult.getLocations()) {
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                            new LatLng(location.getLatitude(),
//                                    location.getLongitude()),15));
//                    mCurrentMarker.remove();
//                    mCurrentMarker = mMap.addMarker(new MarkerOptions()
//                            .title("My location")
//                            .position(new LatLng(location.getLatitude(),
//                                    location.getLongitude())));
//                }
//
//            }
//        };
//
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        getLocationPermission();
        Intent intent = new Intent(this, LocationBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        //getCurrentLocation();
        //mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(50, 82.6);
       // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
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
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        Intent intent = new Intent(this, LocationBackgroundService.class);
        startService(intent);
    }

    private void getCurrentLocation()
    {
        if (mMap == null)
        {
            return;
        }
        try {
            if (mLocationPermissionGranted) {

                mFusedLocationClient.getLastLocation().addOnCompleteListener(this,
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful())
                                {
                                    mLastLocation = task.getResult();
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(mLastLocation.getLatitude(),
                                                    mLastLocation.getLongitude()),15));
                                    mCurrentMarker = mMap.addMarker(new MarkerOptions()
                                            .title("My location")
                                            .position(new LatLng(mLastLocation.getLatitude(),
                                                    mLastLocation.getLongitude())));
                                }
                                else {
                                    Log.d("TAXITEST","Current location is null. Using defaults");
                                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom());
                                }
                                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                                createLocationRequest();

                            }
                        });
            }
        }
        catch (SecurityException ex)
        {
            Log.e("TAXITEST","Security exception");
        }
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(15000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        startLocationUpdates();
    }

    private void startLocationUpdates() {

        try {

            Intent intent = new Intent(this,TrackReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,15,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            //mFusedLocationClient.requestLocationUpdates(mLocationRequest,pendingIntent);
//            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
//                    mLocationCallback,
//                    null /* Looper */);

        }
        catch (SecurityException ex)
        {

        }

    }

}

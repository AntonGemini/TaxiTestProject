package com.sassaworks.taxitestproject.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.sassaworks.taxitestproject.R;
import com.sassaworks.taxitestproject.database.AppDatabase;
import com.sassaworks.taxitestproject.database.AppExecutor;
import com.sassaworks.taxitestproject.database.LocationRoute;

import java.util.Date;

public class LocationBackgroundService extends Service implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener{

    GoogleApiClient mLocationClient;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    public static final String ACTION_LOCATION_BROADCAST = LocationBackgroundService.class.getName() + "TrackBroadcast";
    public static final String LON_DATA = "longititude";
    public static final String LAT_DATA = "latitude";

    private static LocationBackgroundService sInstance = null;

    SharedPreferences sharedPreferences;
    AppDatabase mDb;

    public LocationBackgroundService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    public static boolean isServiceRun() {
        return sInstance != null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        //Создаем foreground сервис чтобы сохранял координаты если приложение закрыто
        Notification notification =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "com.sassaworks.taxitestproject";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            notification = new Notification.Builder(this,NOTIFICATION_CHANNEL_ID )
                    .setContentTitle("Taxi")
                    .setContentText("Taxi")
                    .setSmallIcon(R.drawable.map_black)
                    .setTicker("ticker")
                    .build();
        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setSmallIcon(R.drawable.map_black)
                    .setContentText("Taxi")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            notification = builder.build();

        }
        mDb = AppDatabase.getInstance(getApplicationContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        startForeground(1,notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final Context context = this;

        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {

                    Log.d("TAXI_SERVICE", String.valueOf(location.getLatitude()));
                    Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
                    intent.putExtra(LAT_DATA, location.getLatitude());
                    intent.putExtra(LON_DATA, location.getLongitude());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                    boolean savingState = sharedPreferences.getBoolean(getString(R.string.saving_state),false);
                    if (savingState) {
                        saveToDatabase(location.getLatitude(),location.getLongitude());
                    }
                }

            }
        };

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationClient.connect();


        return START_NOT_STICKY;
    }

    private void startLocationUpdates() {

        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);

        }
        catch (SecurityException ex)
        {

        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sInstance = null;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        sharedPreferences.edit().putBoolean(getString(R.string.saving_state),false).apply();
    }


    private void saveToDatabase(double latitude, double longitude)
    {
        int routeNumber  = sharedPreferences.getInt(getString(R.string.route_number),0);
        long longDate = sharedPreferences.getLong(getString(R.string.saving_date),0);
        String routeName = "Маршрут " + routeNumber;
        final LocationRoute route = new LocationRoute(routeName,latitude,longitude,new Date(longDate));
        AppExecutor.getInstance().getDbExecutor().execute(() -> {
            mDb.routeDao().insertRoute(route);
        });
    }
}

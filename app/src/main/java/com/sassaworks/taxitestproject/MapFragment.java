package com.sassaworks.taxitestproject;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sassaworks.taxitestproject.database.AppDatabase;
import com.sassaworks.taxitestproject.database.AppExecutor;
import com.sassaworks.taxitestproject.database.GetRouteViewModel;
import com.sassaworks.taxitestproject.database.GetRouteViewModelFactory;
import com.sassaworks.taxitestproject.database.LocationRoute;
import com.sassaworks.taxitestproject.service.LocationBackgroundService;

import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static double LAT = 0;
    private static double LON = 0;

    MapView mMap;
    private GoogleMap mGoogleMap;
    private Marker mCurrentMarker = null;
    private static boolean mIsTracking = false;
    private AppDatabase mDb;
    private int mRouteCount = 0;
    private Date mRouteDate;
    private Polyline mPolyline;
    private static Bundle savedState;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container,
                false);
        mMap = v.findViewById(R.id.mapView);
        mMap.onCreate(savedInstanceState);
        mMap.onResume();// needed to get the map to immediately


        if (savedInstanceState!=null)
        {
            mParam1 = savedState.getString(ARG_PARAM1,"1");
        }

        mDb = AppDatabase.getInstance(getContext());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        FloatingActionButton fab = v.findViewById(R.id.startTracking);
        FloatingActionButton removeTrack = v.findViewById(R.id.removeTracks);

//        if (!sharedPreferences.getBoolean(getString(R.string.saving_state), false)) {
//            fab.setImageResource(R.drawable.ic_play);
//        } else {
//            fab.setImageResource(R.drawable.ic_stop);
//        }
        if (mIsTracking)
        {
            fab.setImageResource(R.drawable.ic_stop);
        }
        else
        {
            fab.setImageResource(R.drawable.ic_play);
        }


        removeTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPolyline!=null)
                {
                    mPolyline.remove();
                }
                mParam1 = "1";
                mListener.onFragmentInteraction();
            }
        });

        fab.setOnClickListener((view) -> {
                mParam1 = "1";
                if (!sharedPreferences.getBoolean(getString(R.string.saving_state),false)) {
                    mIsTracking = true;
                    mRouteDate = new Date();
                    mRouteCount = sharedPreferences.getInt(getString(R.string.route_number), 1) + 1;
                    sharedPreferences.edit().putInt(getString(R.string.route_number), mRouteCount).apply();
                    sharedPreferences.edit().putBoolean(getString(R.string.saving_state),true).apply();
                    sharedPreferences.edit().putLong(getString(R.string.saving_date),mRouteDate.getTime()).apply();
                    fab.setImageResource(R.drawable.ic_stop);

                }
                else {
                    mIsTracking = false;
                    sharedPreferences.edit().putBoolean(getString(R.string.saving_state),false).apply();
                    fab.setImageResource(R.drawable.ic_play);
                }
        });

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double latitude = intent.getDoubleExtra(LocationBackgroundService.LAT_DATA,0);
                double longitude = intent.getDoubleExtra(LocationBackgroundService.LON_DATA,0);
                LAT = latitude;
                LON = longitude;
                if (!mParam1.equals("route")) {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(latitude,
                                    longitude), 15));
                    if (mCurrentMarker != null) mCurrentMarker.remove();
                    mCurrentMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .title("My location")
                            .position(new LatLng(latitude,
                                    longitude)));
                }
            }
        },new IntentFilter(LocationBackgroundService.ACTION_LOCATION_BROADCAST));

        mMap.getMapAsync(this);

        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        savedState= new Bundle();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LAT, LON),15));
        mCurrentMarker = mGoogleMap.addMarker(new MarkerOptions()
                .title("My location")
                .position(new LatLng(LAT,
                        LON)));

        if (mListener != null)
            mListener.onFragmentInteraction();



        if (mParam1.equals("route")&&getActivity()!= null)
        {
            GetRouteViewModelFactory factory = new GetRouteViewModelFactory(mDb,mParam2);
            GetRouteViewModel viewModel = ViewModelProviders.of(this,factory)
                    .get(GetRouteViewModel.class);
            viewModel.getRoute().observe(this, new Observer<List<LocationRoute>>() {
                @Override
                public void onChanged(@Nullable List<LocationRoute> locationRoutes) {
                    viewModel.getRoute().removeObserver(this);
                    drawSelectedRoute(locationRoutes);
                }
            });

        }
    }

    void drawSelectedRoute(List<LocationRoute> lr)
    {
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.geodesic(true).color(Color.RED).width(5);
        polyOptions.startCap(new RoundCap());
        polyOptions.endCap(new RoundCap());
        if (lr.size()>0) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lr.get(0).getLatitude(), lr.get(0).getLongitude()), 15));
        }

        for (LocationRoute item : lr)
        {
            LatLng latLng = new LatLng(item.getLatitude(), item.getLongitude());
            polyOptions.add(latLng);
        }
        mPolyline = mGoogleMap.addPolyline(polyOptions);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_PARAM1,mParam1);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

}

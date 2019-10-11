package liveReports.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import liveReports.activities.MainActivity;
import liveReports.livereports.R;
import liveReports.utils.LocationHelper;

public class MapFragment extends Fragment {

    private static final String TAG = "MapFragment";
    GoogleMap mMap;
    private boolean locationPermissionGranted;
    private static final int PERMISSION_REQ_ACCESS_FINE_LOCATION = 9003;
    private LocationHelper locationHelper;
    private Handler handler;
    private Runnable runnable;
    private LatLng latLng;
    private FloatingActionButton fab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
//        locationHandler = LocationHandler.getInstance();
        Log.d(TAG, "onCreate: called");
        locationHelper = new LocationHelper(getActivity());
        getLocationPermission();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_frg);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
            }
        });

        fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).moveToPostFragment();
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: called");
//        if(locationHandler.getLocationPermission(this)){
        if(locationHelper.getLocationPermission()) {
//            locationHandler.getCurrentLocation(/*this*/);
            locationPermissionGranted = true;
            initMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        Log.d(TAG, "onRequestPermissionsResult: before if");
        if (requestCode == PERMISSION_REQ_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        locationPermissionGranted = false;
                        return;
                    }
                }
                locationPermissionGranted = true;
                Log.d(TAG, "onRequestPermissionsResult: calling fused");
//                getLastKnownLocation();
                initMap();
            } else {
                Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initMap() {
//        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
        if(locationPermissionGranted) {
            Log.d(TAG, "initMap: locationPermissionGranted is true");
            getCurrentLocation();
            if(latLng != null) {
                Log.d(TAG, "initMap: latlng " + latLng);
            }
        }
    }

    private void getCurrentLocation() {
        if(locationPermissionGranted) {
            Log.d(TAG, "getCurrentLocation: ");
//            LocationHandler.getCurrentLocation(this);
//            LatLng latLng = LocationHandler.getCurrentLatlng();
            locationHelper.setLatlng();

            runnable = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: called");
                    do {
//                        Log.d(TAG, "run: in loop");
                        latLng = locationHelper.getCurrentLatlng();
                    } while (latLng == null);
                    if(latLng != null)
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mMap.setMyLocationEnabled(true);
                                moveCamera(latLng, 15f);
                            }
                        });
//                    handler.post(runnable);
                }
            };

            Thread t = new Thread(runnable);
            t.start();
//            handler.postDelayed(runnable, 1000);
//            handler.post(runnable);
            if(latLng != null) {
                Log.d(TAG, "getCurrentLocation: latlng not null");
//                moveCamera(latLng, 15f);
            }
            //TODO zoom variable
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

    }
}
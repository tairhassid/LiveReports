package liveReports.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

import liveReports.activities.PostReportActivity;
import liveReports.bl.Report;
import liveReports.data.ReportData;
import liveReports.livereports.R;
import liveReports.utils.CallbacksHandler;
import liveReports.utils.LocationHelper;

public class MapFragment extends Fragment {

    private static final String TAG = "MapFragment";
    private static final float ZOOM = 15f;
    public static final double DIF = 0.005;
    private GoogleMap mMap;
    private boolean locationPermissionGranted;
    private static final int PERMISSION_REQ_ACCESS_FINE_LOCATION = 9003;
    private LocationHelper locationHelper;
    private Handler handler;
    private Runnable runnable;
    private LatLng latLng;
    private FloatingActionButton fab;
    private Context context;
    private ReportData reportData;
    private GeoPoint currentCameraPos;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        reportData = new ReportData();
        Log.d(TAG, "onCreate: called");
        locationHelper = new LocationHelper(getActivity());
//        startLocationService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_frg);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "onMapReady: ");
                mMap = googleMap;
                setOnCameraMoveListener();
//                setOnMarkerClickListener();
            }
        });

        getLocationPermission();

        fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ((MainActivity)getActivity()).moveToPostFragment();
                Intent intent = new Intent(getActivity(), PostReportActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void setOnMarkerClickListener() {
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                return true;
            }
        });
    }

    private void setOnCameraMoveListener() {
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                GeoPoint center =
                        new GeoPoint(mMap.getCameraPosition().target.latitude,
                                mMap.getCameraPosition().target.longitude);
                if(distance(center, currentCameraPos) >= DIF) {
                    Log.d(TAG, "onCameraMove: center=" +center);
                    reportData.getReportsWithinArea(currentCameraPos, DIF, new CallbacksHandler<List<Report>>() {

                        @Override
                        public void onCallback(List<Report> callbackObject) {
                            for(Report report : callbackObject) {
                                Log.d(TAG, "onCallback: from getReportsWithinArea");
                                Marker marker = mMap.addMarker(new MarkerOptions().position(
                                        new LatLng(report.getGeoPoint().getLatitude(),report.getGeoPoint().getLongitude())));
                            }
                        }
                    });
                    currentCameraPos = center;
                }
            }
        });
    }

    private double distance(GeoPoint newPos, GeoPoint oldPos) {
        double ac = Math.abs(newPos.getLongitude() - oldPos.getLongitude());
        double cb = Math.abs(newPos.getLatitude() - oldPos.getLatitude());

        return Math.hypot(ac, cb);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: called");
        if(locationHelper.getLocationPermission()) {
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
                initMap();
            } else {
                Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initMap() {
        Log.d(TAG, "initMap: called");
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
            locationHelper.setLatLng(new CallbacksHandler<LatLng>() {

                @Override
                public void onCallback(LatLng callbackObject) {
                    Log.d(TAG, "onCallback: latlng = " + callbackObject);
                    mMap.setMyLocationEnabled(true);
                    moveCamera(callbackObject, ZOOM);
                }
            });
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        currentCameraPos = new GeoPoint(latLng.latitude, latLng.longitude);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}

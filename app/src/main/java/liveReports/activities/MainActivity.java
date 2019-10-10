package liveReports.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import liveReports.livereports.R;
import liveReports.utils.LocationService;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "MainActivity";
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int PERMISSION_REQ_ACCESS_FINE_LOCATION = 9003;
    private LatLng latLngTest;
    private LocationService locationService;
    private Handler handler;
    private Runnable runnable;
    private LatLng latLng;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
//        locationService = LocationService.getInstance();
        Log.d(TAG, "onCreate: called");
        locationService = new LocationService(this);
        getLocationPermission();

    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: called");
//        if(locationService.getLocationPermission(this)){
        if(locationService.getLocationPermission()) {
//            locationService.getCurrentLocation(/*this*/);
            locationPermissionGranted = true;
            initMap();
        }
    }
//
//    private void getLocationPermission() {
//        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "getLocationPermission: " + locationPermissionGranted);
//            locationPermissionGranted = true;
//            initMap();
////            getLastKnownLocation();
//        } else {
//            Log.d(TAG, "getLocationPermission: in else, requesting");
//            ActivityCompat.requestPermissions(this,
//                    new String[]{
//                            android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSION_REQ_ACCESS_FINE_LOCATION);
//        }
//    }

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
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if(locationPermissionGranted) {
            Log.d(TAG, "initMap: locationPermissionGranted is true");
            getCurrentLocation();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

//    private void getCurrentLocation() {
//        Log.d(TAG, "getCurrentLocation: called");
//        try {
//            if(locationPermissionGranted) {
//                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//                Task location = fusedLocationProviderClient.getLastLocation();
//                location.addOnCompleteListener(new OnCompleteListener() {
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        if(task.isSuccessful()) {
//                            Log.d(TAG, "onComplete: found location");
//                            Location currentLocation = (Location) task.getResult();
//                            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                            latLngTest = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                            moveCamera(latLng, 15f);
//                            //TODO zoom variable
//                        }
//                        else
//                            Log.d(TAG, "onComplete: location is null");
//                    }
//                });
//            }
//        } catch (SecurityException e) {
//            Log.e(TAG, "getCurrentLocation: SecurityException " + e.getMessage() );
//        }
//
//
//    }

    private void getCurrentLocation() {
        if(locationPermissionGranted) {
            Log.d(TAG, "getCurrentLocation: ");
//            LocationService.getCurrentLocation(this);
//            LatLng latLng = LocationService.getCurrentLatlng();
            locationService.setLatlng();

            runnable = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: called");
                    do {
//                        Log.d(TAG, "run: in loop");
                        latLng = locationService.getCurrentLatlng();
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
                moveCamera(latLng, 15f);
            }
            //TODO zoom variable
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

    }
}

package liveReports.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationHelper {

    private static final String TAG = "LocationHelper";
    private final long INTERVAL = 4000;
    private final long FASTEST_INTERVAL = 2000;

    private static LatLng currentLatLng;
    private LocationCallback locationCallback;

    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int PERMISSION_REQ_ACCESS_FINE_LOCATION = 9003;
    private Activity callingActivity;

    public LocationHelper(Activity callingActivity) {
        this.callingActivity = callingActivity;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(callingActivity);
        Log.d(TAG, "LocationHelper: " + callingActivity.toString());
    }

    //calls the callback function when location is available
    private void initLocationCallback(final CallbacksHandler<LatLng> callbacksHandler) {
        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "onLocationResult: ");
                super.onLocationResult(locationResult);
                stopLocationUpdates();
                if(locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLocations().get(0);
                    Log.d(TAG, "onLocationResult: got location " + location);
                    callbacksHandler.onCallback(new LatLng(location.getLatitude(), location.getLongitude()));
                } else {
                    callbacksHandler.onCallback(null);
                }
            }
        };
    }

    public boolean getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(callingActivity.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocationPermission: " + locationPermissionGranted);
            locationPermissionGranted = true;
        } else {
            Log.d(TAG, "getLocationPermission: in else, requesting");
            ActivityCompat.requestPermissions(callingActivity,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQ_ACCESS_FINE_LOCATION);
        }
        Log.d(TAG, "getLocationPermission: locationPermissionGranted=" + locationPermissionGranted);
        return locationPermissionGranted;
    }

    public void setLatLng(final CallbacksHandler<LatLng> callbacksHandler) {
        Task location = fusedLocationProviderClient.getLastLocation();
        location.addOnSuccessListener(callingActivity, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    setCurrentLatLng(latLng);
                    Log.d(TAG, "onSuccess: " + getCurrentLatlng());
                    callbacksHandler.onCallback(getCurrentLatlng());
                }
                else {
                    Log.d(TAG, "onSuccess: location is null");
                    initLocationCallback(callbacksHandler);
                    //to try again later
                    fusedLocationProviderClient.requestLocationUpdates(createLocationRequest(), locationCallback, null );
                    callbacksHandler.onCallback(null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callbacksHandler.onCallback(null);
            }
        });
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    //currentLatLng is a static variable
    private void setCurrentLatLng(LatLng latlng) {
        this.currentLatLng = latlng;
    }

    public LatLng getCurrentLatlng() {
        return currentLatLng;
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }


}

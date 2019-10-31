package liveReports.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationHelper {

    private static final String TAG = "LocationHelper";

    private static LatLng currentLatLng;

    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int PERMISSION_REQ_ACCESS_FINE_LOCATION = 9003;
    private Activity callingActivity;

    public LocationHelper(Activity callingActivity) {
        this.callingActivity = callingActivity;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(callingActivity);
        Log.d(TAG, "LocationHelper: " + callingActivity.toString());
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
        Log.d(TAG, "setLatLng: called " + currentLatLng);
        Task location = fusedLocationProviderClient.getLastLocation();
        location.addOnSuccessListener(callingActivity, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.d(TAG, "onSuccess: called");
                if (location != null) {
                    Log.d(TAG, "getLastKnownLocation: location " + location );
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    setCurrentLatLng(latLng);
                    Log.d(TAG, "onSuccess: " + getCurrentLatlng());
                    callbacksHandler.onCallback(getCurrentLatlng());
                }
            }
        });
    }

    //currentLatLng is a static variable
    private void setCurrentLatLng(LatLng latlng) {
        this.currentLatLng = latlng;
    }

    public LatLng getCurrentLatlng() {
//        Log.d(TAG, "getCurrentLatlng: " + currentLatLng);
        return currentLatLng;
    }


}

package liveReports.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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

    private static LatLng currentLatlng;

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

    public void setLatlng() {
        Log.d(TAG, "setLatlng: called " + currentLatlng);
        Task location = fusedLocationProviderClient.getLastLocation();
        location.addOnSuccessListener(callingActivity, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.d(TAG, "onSuccess: called");
                if (location != null) {
                    Log.d(TAG, "getLastKnownLocation: location " + location );

                    setCurrentLatlng(new LatLng(location.getLatitude(), location.getLongitude()));
                    Log.d(TAG, "onSuccess: " + currentLatlng);
                }
            }
        });
    }

    private void setCurrentLatlng(LatLng latlng) {
        this.currentLatlng = latlng;
    }

    public LatLng getCurrentLatlng() {
        return currentLatlng;
    }

    /*private void startLocationService() {
        if(!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(callingActivity, LocationService.class);
            Log.d(TAG, "startLocationService: calling: " + callingActivity.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                callingActivity.startForegroundService(serviceIntent);
                Log.d(TAG, "startLocationService: ");

            } else {
                callingActivity.startService(serviceIntent);
                Log.d(TAG, "startLocationService: ");

            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) callingActivity.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if("liveReports.utils.LocationService".equals(serviceInfo.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: already runnning");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: not running");
        return false;
    }*/

}

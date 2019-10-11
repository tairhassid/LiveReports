package liveReports.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationHelper {

    private static final String TAG = "LocationHelper";
    private static LatLng currentLatlng;
    private /*static*/ boolean locationPermissionGranted;
    private /*static*/ FusedLocationProviderClient fusedLocationProviderClient;
    private static final int PERMISSION_REQ_ACCESS_FINE_LOCATION = 9003;
    private final long INTERVAL = 4000;
    private final long FASTEST_INTERVAL = 2000;
    private Activity callingActivity;

    public LocationHelper(Activity callingActivity) {
        this.callingActivity = callingActivity;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(callingActivity);

//        createLocationRequest();
    }

    private void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    public boolean getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(callingActivity.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocationPermission: " + locationPermissionGranted);
            locationPermissionGranted = true;
            setLatlng();
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
//        try {
//            if(locationPermissionGranted) {

                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnSuccessListener(callingActivity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d(TAG, "onSuccess: called");
                        if (location != null) {
                            Log.d(TAG, "getLastKnownLocation: location " + location );

//                            currentLatlng = new LatLng(location.getLatitude(), location.getLongitude());
                            setCurrentLatlng(new LatLng(location.getLatitude(), location.getLongitude()));
                            Log.d(TAG, "onSuccess: " + currentLatlng);
                        }
                    }
                });
//                location.addOnCompleteListener(new OnCompleteListener<Location>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Location> task) {
//                        if(task.isSuccessful()) {
//                            Log.d(TAG, "onComplete: found location");
//                            Location currentLocation = task.getResult();
//                            currentLatlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                            Log.d(TAG, "onComplete: location = " + currentLatlng.latitude + "  " + currentLatlng.longitude) ;
////                            moveCamera(latLng, 15f);
////                            TODO zoom variable
//                        }
//                        else
//                            Log.d(TAG, "onComplete: location is null");
//                    }
//                });
//            }
//        } catch (SecurityException e) {
//            Log.e(TAG, "setLatlng: SecurityException " + e.getMessage() );
//        }
//        Log.d(TAG, "setLatlng: latlng= " + currentLatlng);

    }

    private void setCurrentLatlng(LatLng latlng) {
        this.currentLatlng = latlng;
    }

    public /*static*/ LatLng getCurrentLatlng() {
        Log.d(TAG, "getCurrentLatlng: " + currentLatlng);
        return currentLatlng;
    }

}

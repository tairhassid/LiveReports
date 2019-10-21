package liveReports.utils;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class LocationService extends Service {

    private static final String TAG = "LocationService";

    private final IBinder binder = new LocalBinder();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final long INTERVAL = 4000;
    private final long FASTEST_INTERVAL = 2000;
    private LatLng currentLatLng;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        getLocation();
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Log.d(TAG, "onCreate: ");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getLocation();
        Log.d(TAG, "onStartCommand: ");
        return START_NOT_STICKY;
    }

    private void getLocation() {
        final LocationRequest locationRequest = createLocationRequest();

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "onLocationResult: ");
                if(locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "onLocationResult: in location service: " + currentLatLng);
                }
            }
        }, Looper.myLooper());
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }

    //https://developer.android.com/guide/components/bound-services
    public class LocalBinder extends Binder {
        public LocationService getServiceInstance() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }

    public LatLng getCurrentLatLng() {
        return currentLatLng;
    }
}

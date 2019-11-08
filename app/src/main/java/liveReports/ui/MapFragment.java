package liveReports.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liveReports.activities.LoginActivity;
import liveReports.activities.PostReportActivity;
import liveReports.activities.WatchReportActivity;
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
    private FloatingActionButton fab;
    private ReportData reportData;
    private GeoPoint currentCameraPos;
    private Map<Marker, Report> markerReportMap;
    private View rootView;
    private ImageView menuView;
    private FirebaseAuth mAuth;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reportData = new ReportData();
        markerReportMap = new HashMap<>();
        Log.d(TAG, "onCreate: called");
        locationHelper = new LocationHelper(getActivity());
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        initFragment();
        getLocationPermission();
        initFab();
        initMenu();
//        initAppBar();

        return rootView;
    }

    private void initMenu() {
        menuView = rootView.findViewById(R.id.menu);

        menuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: menuView");
                showPopup(view);
            }
        });
    }

    public void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.inflate(R.menu.map_fragment_menu);
        Menu menu = popupMenu.getMenu();
        if(!checkLoggedIn()) {
            MenuItem logout = menu.getItem(0);
            MenuItem login = menu.getItem(1);
            logout.setVisible(false);
            login.setVisible(true);
            //TODO need to change the switch case!!!
        }
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Log.d(TAG, "onMenuItemClick: ");
                switch (menuItem.getItemId()) {
                    case R.id.log_out:
                        signOut();
                        return true;

                    case R.id.log_in:
                        Log.d(TAG, "onMenuItemClick: going to login page");
                        moveToLoginActivity();
                        return true;

                    default:
                        return false;
                }
            }
        });

    }

    private void moveToLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    private void signOut() {
        mAuth.signOut();
    }

    private void initFab() {
        fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkLoggedIn()) {
                    Intent intent = new Intent(getActivity(), PostReportActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "User must be logged in to share reports", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkLoggedIn() {
        FirebaseAuth auth  = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if(user != null) {
            return true;
        }
        return false;
    }

    private void initFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_frg);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "onMapReady: ");
                mMap = googleMap;
                setOnCameraMoveListener();
                setOnMarkerClickListener();
            }
        });
    }

    private void setOnMarkerClickListener() {
        Log.d(TAG, "setOnMarkerClickListener: called");
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Report report = markerReportMap.get(marker);
                Log.d(TAG, "onMarkerClick: report" + report);
                Intent intent = new Intent(getActivity(), WatchReportActivity.class);
                intent.putExtra("report", report);
                startActivity(intent);
                return true;
            }
        });
    }

    private void setOnCameraMoveListener() {
        Log.d(TAG, "setOnCameraMoveListener: ");
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                GeoPoint center =
                        new GeoPoint(mMap.getCameraPosition().target.latitude,
                                mMap.getCameraPosition().target.longitude);
                if(distance(center, currentCameraPos) >= DIF) {
                    Log.d(TAG, "onCameraMove: center=" +center);
                    getReportsWithinArea();
                    currentCameraPos = center;
                }
            }
        });
    }

    private void getReportsWithinArea() {
        reportData.getReportsWithinArea(currentCameraPos, DIF, new CallbacksHandler<List<Report>>() {

            @Override
            public void onCallback(List<Report> callbackObject) {
                Log.d(TAG, "onCallback: from getReportsWithinArea");
                for(Report report : callbackObject) {
                    Marker marker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(report.getGeoPoint().getLatitude(),report.getGeoPoint().getLongitude())));
                    markerReportMap.put(marker, report);
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
                    getReportsWithinArea();

                }
            });
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        Log.d(TAG, "moveCamera: ");
        currentCameraPos = new GeoPoint(latLng.latitude, latLng.longitude);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}

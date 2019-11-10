package liveReports.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

    //constants
    private static final String TAG = "MapFragment";
    private static final float ZOOM = 15f;
    private static final double DIF = 0.005;
    private static final int PERMISSION_REQ_ACCESS_FINE_LOCATION = 9003;

    //ui variables
    private FloatingActionButton fab;
    private View rootView;
    private ImageView menuView;
    private ProgressBar progressBar;

    //variables
    private boolean locationPermissionGranted;
    private boolean hasLatLng;
    private Map<Marker, Report> markerReportMap;
    private LocationHelper locationHelper;
    private ReportData reportData;

    private FirebaseAuth mAuth;
    private GeoPoint currentCameraPos;
    private GoogleMap mMap;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reportData = new ReportData();
        markerReportMap = new HashMap<>();
        locationHelper = new LocationHelper(getActivity());
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        progressBar = rootView.findViewById(R.id.progress_bar_image);

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
                showPopup(view);
            }
        });
    }

    private void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.inflate(R.menu.map_fragment_menu);
        Menu menu = popupMenu.getMenu();
        if(!checkLoggedIn()) {
            MenuItem logout = menu.getItem(0);
            MenuItem login = menu.getItem(1);
            logout.setVisible(false);
            login.setVisible(true);
        }
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.log_out:
                        signOut();
                        return true;

                    case R.id.log_in:
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
                    if(hasLatLng) {
                        moveToPostReportActivity();
                    } else {
                        Toast.makeText(getActivity(), "Location has to be available in order to post reports", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "User must be logged in to share reports", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void moveToPostReportActivity() {
        Intent intent = new Intent(getActivity(), PostReportActivity.class);
        Bundle bundle = ActivityOptions.makeCustomAnimation(
                getActivity(),
                R.anim.slide_enter_diagonal,
                R.anim.slide_exit_diagonal)
                .toBundle();
        startActivity(intent, bundle);
    }

    private boolean checkLoggedIn() {
        FirebaseAuth auth  = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return false;
        }
        return true;
    }

    private void initFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_frg);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                currentCameraPos = new GeoPoint(-33.852, 151.211);
                mMap = googleMap;
                setOnCameraMoveListener();
                setOnMarkerClickListener();
            }
        });
    }

    private void setOnMarkerClickListener() {
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Report report = markerReportMap.get(marker);
                Log.d(TAG, "onMarkerClick: report" + report);
                moveToWatchReportActivity(report);
                return true;
            }
        });
    }

    private void moveToWatchReportActivity(Report report) {
        Intent intent = new Intent(getActivity(), WatchReportActivity.class);
        intent.putExtra("report", report);

        Bundle bundle = ActivityOptions.makeCustomAnimation(
                getActivity(),
                R.anim.slide_enter_right,
                R.anim.slide_exit_left)
                .toBundle();
        startActivity(intent, bundle);
    }

    private void setOnCameraMoveListener() {
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                GeoPoint center =
                        new GeoPoint(mMap.getCameraPosition().target.latitude,
                                mMap.getCameraPosition().target.longitude);
                if(distance(center, currentCameraPos) >= DIF) {
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
                Log.d(TAG, "getReportsWithinArea onCallback");
                for(Report report : callbackObject) {
                    Marker marker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(report.getGeoPoint().getLatitude(),report.getGeoPoint().getLongitude())));
                    markerReportMap.put(marker, report);
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private double distance(GeoPoint newPos, GeoPoint oldPos) {
        double ac = Math.abs(newPos.getLongitude() - oldPos.getLongitude());
        double cb = Math.abs(newPos.getLatitude() - oldPos.getLatitude());

        return Math.hypot(ac, cb);

    }

    private void getLocationPermission() {
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
        if (requestCode == PERMISSION_REQ_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        locationPermissionGranted = false;
                        Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                }
                locationPermissionGranted = true;
                initMap();
            }
        }
    }

    private void initMap() {
        if(locationPermissionGranted) {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if(locationPermissionGranted) {
            locationHelper.setLatLng(new CallbacksHandler<LatLng>() {

                @Override
                public void onCallback(LatLng callbackObject) {
                    Log.d(TAG, "onCallback: from setLatLng");
                    if(callbackObject != null) {
                        mMap.setMyLocationEnabled(true);
                        moveCamera(callbackObject);
                        getReportsWithinArea();
                        hasLatLng = true;
                    } else {
                        Toast.makeText(getContext(), "Couldn't get current location", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        hasLatLng = false;
                    }

                }
            });
        }
    }

    private void moveCamera(LatLng latLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
        currentCameraPos = new GeoPoint(latLng.latitude, latLng.longitude);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}

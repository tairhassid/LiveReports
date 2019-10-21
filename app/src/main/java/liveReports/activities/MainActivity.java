package liveReports.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import liveReports.bl.PostReport;
import liveReports.livereports.R;
import liveReports.ui.AddImageFragment;
import liveReports.ui.MapFragment;
import liveReports.ui.PostReportFragment;


public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private static final String TAG = "MainActivity";
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            MapFragment firstFragment = new MapFragment();
            currentFragment = firstFragment;

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction().
                    add(R.id.fragment_container, firstFragment).commit();
        }
    }

    public void moveToPostFragment() {
        PostReportFragment postReportFragment = new PostReportFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, postReportFragment)
                .addToBackStack("MapFragment")
                .commit();

        currentFragment = postReportFragment;
    }

    public void moveToMapFragment() {
        MapFragment mapFragment = new MapFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .commit();

        currentFragment = mapFragment;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: ");
        currentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        locationPermissionGranted = false;
//        Log.d(TAG, "onRequestPermissionsResult: before if");
//        if (requestCode == PERMISSION_REQ_ACCESS_FINE_LOCATION) {
//            if (grantResults.length > 0) {
//                for (int i = 0; i < grantResults.length; i++) {
//                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                        locationPermissionGranted = false;
//                        return;
//                    }
//                }
//                locationPermissionGranted = true;
//                Log.d(TAG, "onRequestPermissionsResult: calling fused");
////                getLastKnownLocation();
//                initMap();
//            } else {
//                Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    public void moveToAddImageFragment() {
        AddImageFragment addImageFragment = new AddImageFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, addImageFragment)
                .commit();

        currentFragment = addImageFragment;
    }
}

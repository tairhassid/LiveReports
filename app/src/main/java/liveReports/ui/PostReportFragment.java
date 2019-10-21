package liveReports.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;

import liveReports.activities.MainActivity;
import liveReports.bl.PostReport;
import liveReports.livereports.R;
import liveReports.utils.LocationHelper;
import liveReports.utils.LocationService;
import liveReports.utils.LocationService.LocalBinder;

public class PostReportFragment extends Fragment {

    //constants
    private static final String TAG = "PostReportFragment";
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSIONS_REQ_CODE = 1;

    //variables
    private LocationHelper locationHelper;
    private LocationService mService;
    private boolean mBound = false;
    private PostReport postReport;
    private ItemSelected itemSelected;
    //UI variables
    private Spinner typeSpinner;
    private View fragmentView;
    private Button submitButton;
    private Button backButton;
    private Button uploadImageButton;
    private EditText reportText;
    private EditText name;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationHelper = new LocationHelper(getActivity());
        postReport = new PostReport();
        Log.d(TAG, "onCreate: called");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        // Bind to LocalService
        Intent intent = new Intent(getActivity(), LocationService.class);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(connection);
        mBound = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_post_report, container, false);

        initSubmitButton();

        backButton = fragmentView.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).moveToMapFragment();
            }
        });
        initTypeSpinner();

        uploadImageButton = fragmentView.findViewById(R.id.btn_upload_photo);
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasAllPermissions()) {
                    ((MainActivity)getActivity()).moveToAddImageFragment();
                } else {
                    getAllPermissions();
                }
            }
        });

        return fragmentView;
    }

    private void getAllPermissions() {
        ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSIONS_REQ_CODE);
    }

    private boolean hasAllPermissions() {
        for(int i=0 ; i<PERMISSIONS.length ; i++) {
            String permissionToCheck = PERMISSIONS[i];
            if(!hasPermission(permissionToCheck)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPermission(String permissionToCheck) {
        int permissionResult = ActivityCompat.checkSelfPermission(getActivity(), permissionToCheck);

        return permissionResult == PackageManager.PERMISSION_GRANTED;
    }

    //taken from: https://developer.android.com/guide/topics/ui/controls/spinner
    private void initTypeSpinner() {
        typeSpinner = fragmentView.findViewById(R.id.type_spinner);
        itemSelected = new ItemSelected();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.types_array,
                android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        typeSpinner.setAdapter(adapter);
        typeSpinner.setOnItemSelectedListener(itemSelected);
    }

    private void initSubmitButton() {
        submitButton = fragmentView.findViewById(R.id.btn_submit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng latLng = mService.getCurrentLatLng();
                name = fragmentView.findViewById(R.id.name_edit_text);
                reportText = fragmentView.findViewById(R.id.report_edit_text);

                postReport.saveReport(name.getText().toString(), itemSelected.getItemId(),
                        reportText.getText().toString(), latLng);
                Log.d(TAG, "onClick: " + latLng.latitude);
                Log.d(TAG, "onClick: item= " + itemSelected.getItem());
            }
        });
    }

    /*private void startLocationService() {
        if(!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(getActivity(), LocationService.class);
            Log.d(TAG, "startLocationService: calling: " + getActivity().toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getActivity().startForegroundService(serviceIntent);
                Log.d(TAG, "startLocationService: startForegroundService ");

            } else {
                getActivity().startService(serviceIntent);
                Log.d(TAG, "startLocationService: startService ");

            }
        }
    }

    private boolean isLocationServiceRunning() {
        Log.d(TAG, "isLocationServiceRunning: called");
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            for(ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if("liveReports.utils.LocationService".equals(serviceInfo.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: already runnning");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: not running");
        return false;
    }*/

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: ");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) iBinder;
            mService = binder.getServiceInstance();
            mBound = true;
        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected: ");
            mBound = false;
        }
    };

    public class ItemSelected implements AdapterView.OnItemSelectedListener {

        private String item;
        private long itemId;

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(TAG, "onItemSelected: " + adapterView.getItemAtPosition(i) + " id= " + l);
            item = (String) adapterView.getItemAtPosition(i);
            itemId = l;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }

        private String getItem() {
            return item;
        }

        private long getItemId() {
            return itemId;
        }
    }




}
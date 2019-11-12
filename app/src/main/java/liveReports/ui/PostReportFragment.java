package liveReports.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.GeoPoint;

import liveReports.activities.MainActivity;
import liveReports.activities.PostReportActivity;
import liveReports.bl.PostManager;
import liveReports.bl.Report;
import liveReports.livereports.R;
import liveReports.utils.AddImagePermissions;
import liveReports.utils.CallbacksHandler;
import liveReports.utils.Constants;
import liveReports.utils.LocationService;
import liveReports.utils.LocationService.LocalBinder;

public class PostReportFragment extends Fragment {

    //constants
    private static final String TAG = "PostReportFragment";

    //variables
    private LocationService mService;
    private GeoPoint geoPoint;
    private boolean mBound = false;
    private ItemSelected itemSelected;
    private AddImagePermissions addImagePermissions;
    private PostManager postManager;
    //UI variables
    private Spinner typeSpinner;
    private View fragmentView;
    private Button submitButton;
    private Button backButton;
    private Button uploadImageButton;
    private EditText reportText;
    private EditText name;
    private ImageView sharedImage;
    private TextView headlineText;
    private TextView errorText;
    private ProgressDialog mProgressDialog;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postManager = PostManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_post_report, container, false);

        name = fragmentView.findViewById(R.id.name_edit_text);
        typeSpinner = fragmentView.findViewById(R.id.type_spinner);
        sharedImage = fragmentView.findViewById(R.id.shared_image);
        headlineText = fragmentView.findViewById(R.id.text_view_headline);
        errorText = fragmentView.findViewById(R.id.error);

        headlineText.setText(R.string.post_fragment_headline);

        initReportText();
        initSubmitButton();
        initBackButton();
        initTypeSpinner();
        initUploadImgBtn();

        setReportFieldsOnView();


        return fragmentView;
    }

    private void initReportText() {
        reportText = fragmentView.findViewById(R.id.report_edit_text);
        reportText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                errorText.setText(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
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
//        getActivity().unbindService(connection);
//        mBound = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(addImagePermissions.hasAllPermissions()) {
            moveToAddImageFragment();
        }
    }

    private void setReportFieldsOnView() {
        Report currentReport = postManager.getCurrentReport();
        name.setText(currentReport.getName());
        typeSpinner.setSelection(currentReport.getOrdinalOfType());
        reportText.setText(currentReport.getReportText());

        String selectedImage = currentReport.getSelectedImage();
        if(!TextUtils.isEmpty(selectedImage)) {
            sharedImage.setImageURI(Uri.parse(selectedImage));
            sharedImage.setRotation(currentReport.getImageRotation());
            sharedImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO
                }
            });
        }

    }

    private void initUploadImgBtn() {
        uploadImageButton = fragmentView.findViewById(R.id.btn_upload_photo);
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: upload image");
                updateCurrentReport(view.getId());
                moveToAddImageFragment();
            }
        });
    }

    private void moveToAddImageFragment() {
        addImagePermissions = new AddImagePermissions(getActivity());
        if(addImagePermissions.hasAllPermissions()) {
            ((PostReportActivity)getActivity()).moveToAddImageFragment();
        } else {
            addImagePermissions.getAllPermissions();
        }
    }

    private void initBackButton() {
        backButton = fragmentView.findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }

    private void goBackToMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
    }

    //taken from: https://developer.android.com/guide/topics/ui/controls/spinner
    private void initTypeSpinner() {
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
                if(hasInternetConnection()) {
                    updateCurrentReport(view.getId());
                    Log.d(TAG, "onClick: " + geoPoint.getLatitude());
                    Log.d(TAG, "onClick: item= " + itemSelected.getItem());
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(R.string.cant_upload_report_message)
                            .setTitle(R.string.cant_upload_report_title)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    //taken from https://developer.android.com/training/monitoring-device-state/connectivity-monitoring
    private boolean hasInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();


    }

    private void updateCurrentReport(int id) {
        if(mBound) {
            geoPoint = mService.getCurrentLatLng();

            postManager.setCurrentReport(name.getText().toString(),
                    itemSelected.getItemId(),
                    reportText.getText().toString(),
                    geoPoint);
            if (id == R.id.btn_submit) {
                saveReportToDatabase();
            }
        }
    }

    private void saveReportToDatabase() {
        if (checkInput()) {
            showProgressDialog();
            postManager.saveCurrentReportToDatabase(getActivity(), new CallbacksHandler<String>() {
                @Override
                public void onCallback(String callbackObject) {
                    hideProgressDialog();
                    if (callbackObject.equals(Constants.SUCCESS)) {
                        goBackToMainActivity();
                    } else {
                        Log.d(TAG, "onCallback: " + callbackObject);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(R.string.cant_upload_report_message)
                                .setTitle(R.string.cant_upload_report_title)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
        }
    }

    private boolean checkInput() {
        boolean valid = true;
        Report currentReport = postManager.getCurrentReport();
        if(TextUtils.isEmpty(currentReport.getName())) {
            name.setError("Required");
            valid = false;
        }

        if(!TextUtils.isEmpty(currentReport.getReportText()) ||
                !TextUtils.isEmpty(currentReport.getSelectedImage())) {
        } else {
            errorText.setText(R.string.blank_report);
            valid = false;
        }
        return valid;
    }

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

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Posting report, please wait");
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unbindService(connection);
        mBound = false;
    }

}

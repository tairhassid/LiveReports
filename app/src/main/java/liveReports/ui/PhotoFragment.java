package liveReports.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import liveReports.activities.PostReportActivity;
import liveReports.livereports.R;
import liveReports.utils.AddImagePermissions;

public class PhotoFragment extends Fragment {

    private static final String TAG = "PhotoFragment";
    private static final int CAMERA_REQ_CODE = 100;
    private View rootView;
    private AddImagePermissions addImagePermissions;
    private Button takePhoto;
    private ImageView imageView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        rootView = inflater.inflate(R.layout.fragment_photo, container, false);
//        imageView = rootView.findViewById(R.id.image_view_photo_taken);
        takePhoto = rootView.findViewById(R.id.btn_take_photo);
//        takePhoto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startCamera();
//            }
//        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
//        startCamera();
    }

//    private void startCamera() {
//        addImagePermissions = new AddImagePermissions(getActivity());
//
//        if(((OLDAddImageFragment)getParentFragment()).getCurrentTabNumber() == 1)
//            if(addImagePermissions.hasPermission(Manifest.permission.CAMERA)) {
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null)
//                    startActivityForResult(cameraIntent, CAMERA_REQ_CODE);
//            } else {
//                Toast.makeText(getActivity(), "You have to allow camera permission", Toast.LENGTH_LONG).show();
//                ((PostReportActivity)getActivity()).moveToPostFragment();
//            }
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        Log.d(TAG, "onActivityResult: ");
//        if(resultCode == Activity.RESULT_OK)
//            if(requestCode == CAMERA_REQ_CODE) {
//                Log.d(TAG, "onActivityResult: photo taken");
//                imageView.setImageURI(data.getData());
//            }
//    }


}

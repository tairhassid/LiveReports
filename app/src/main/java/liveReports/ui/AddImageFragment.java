package liveReports.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import liveReports.activities.PostReportActivity;
import liveReports.bl.PostManager;
import liveReports.livereports.R;
import liveReports.utils.AddImagePermissions;


public class AddImageFragment extends Fragment {


    private static final String TAG = "AddImageFragment";
    private static final int IMAGE_PICK_CODE = 101;
    private static final int CAMERA_REQ_CODE = 102;

    private AddImagePermissions addImagePermissions;
    private View rootView;
    private ImageView imageView;
    private Button addImgBtn;
    private Button takePhotoBtn;
    private ImageView imageNext;
    private PostManager postManager;
    private String selectedImage;
    private String currentPhotoPath;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.d(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        init();
        postManager = PostManager.getInstance();
//        Log.d(TAG, "onCreateView: " + postManager.getCurrentReport());
        return rootView;
    }

    private void init() {
        addImagePermissions = new AddImagePermissions(getActivity());
        imageView = rootView.findViewById(R.id.image_view_preview);

        initAddImgBtn();
        initTakePhotoBtn();
        initCloseImgView();
        initImgNext();
    }

    private void initImgNext() {
        imageNext = rootView.findViewById(R.id.image_next);
        imageNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.d(TAG, "onClick: next");
                PostManager.getInstance().getCurrentReport().setSelectedImage(selectedImage);
                getFragmentManager().popBackStackImmediate();
            }
        });
    }

    private void initCloseImgView() {
        ImageView close = rootView.findViewById(R.id.image_view_close_add_image);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.d(TAG, "onClick: close");
                ((PostReportActivity)getActivity()).moveToPostFragment();
            }
        });
    }

    private void initTakePhotoBtn() {
        takePhotoBtn = rootView.findViewById(R.id.btn_take_photo);
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startCamera();
                dispatchTakePictureIntent();
            }
        });
    }

    private void initAddImgBtn() {
        addImgBtn = rootView.findViewById(R.id.btn_add_image);
        addImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addImagePermissions.hasAllPermissions()) {
                    pickImageFromGallery();
                } else {
                    addImagePermissions.getAllPermissions();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.d(TAG, "onResume: ");
//        addImagePermissions = new AddImagePermissions(getActivity());
//
//        if(addImagePermissions.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//            pickImageFromGallery();
//        } else {
//            //TODO restart OLDAddImageFragment that will ask permissions again
//        }


    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void startCamera() {
        addImagePermissions = new AddImagePermissions(getActivity());

            if(addImagePermissions.hasPermission(Manifest.permission.CAMERA)) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null)
                    startActivityForResult(cameraIntent, CAMERA_REQ_CODE);
            } else {
                Toast.makeText(getActivity(), "You have to allow camera permission", Toast.LENGTH_LONG).show();
//                ((PostReportActivity)getActivity()).moveToPostFragment();
            }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_CODE) {
//                Log.d(TAG, "onActivityResult: " +data.getData());
//                imageView.setImageURI(data.getData());
                Uri imageUri = data.getData();
                selectedImage = imageUri.toString();
                Picasso.get().load(imageUri).fit().into(imageView);
//                imageNext.setVisibility(View.VISIBLE);
            } else if (requestCode == CAMERA_REQ_CODE) {
//                Log.d(TAG, "onActivityResult: photo taken");
                imageView.setImageURI(Uri.parse(currentPhotoPath));
                selectedImage = currentPhotoPath;

//                Bundle extras = data.getExtras();
//                Bitmap imageBitmap = (Bitmap) extras.get("data");
//                imageView.setImageBitmap(imageBitmap);
            }
            imageNext.setVisibility(View.VISIBLE);
        }
    }

    //taken from https://developer.android.com/training/camera/photobasics
    private void dispatchTakePictureIntent() {
        if(addImagePermissions.hasPermission(Manifest.permission.CAMERA)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(getActivity(),
                            "project.livereports.fileprovider",
                            photoFile);
//                    Log.d(TAG, "dispatchTakePictureIntent: uri" + photoURI.toString());
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_REQ_CODE);
                }
            }
        } else {
            Toast.makeText(getActivity(), "You have to allow camera permission", Toast.LENGTH_LONG).show();
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        currentPhotoPath = Uri.fromFile(image).toString();

//        Log.d(TAG, "createImageFile: currentPhotoPath " + Uri.fromFile(image).toString());
        return image;
    }
}

package liveReports.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import androidx.exifinterface.media.ExifInterface;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import liveReports.activities.PostReportActivity;
import liveReports.bl.PostManager;
import liveReports.bl.Report;
import liveReports.livereports.R;
import liveReports.utils.AddImagePermissions;


public class AddImageFragment extends Fragment {

    //consts
    private static final String TAG = "AddImageFragment";
    private static final int IMAGE_PICK_CODE = 101;
    private static final int CAMERA_REQ_CODE = 102;
    private static final String HEADLINE = "Add a Photo";

    //vars
    private AddImagePermissions addImagePermissions;
    private String selectedImage;
    private String currentPhotoPath;
    private File photoFile;
    private float totalRotation;

    //ui vars
    private View rootView;
    private ImageView imageView;
    private ProgressBar progressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        init();
        return rootView;
    }

    private void init() {
        addImagePermissions = new AddImagePermissions(getActivity());
        imageView = rootView.findViewById(R.id.image_view_preview);

        TextView headlineText = rootView.findViewById(R.id.text_view_headline);
        headlineText.setText(HEADLINE);

        progressBar = rootView.findViewById(R.id.progress_bar_image);
        progressBar.setVisibility(View.GONE);

        PostManager.getInstance().getCurrentReport().setSelectedImage("");

        initAddImgBtn();
        initTakePhotoBtn();
        initCloseImgView();
    }

    private void initRotateBtn() {
        Button rotateBtn = rootView.findViewById(R.id.rotate);
        rotateBtn.setVisibility(View.VISIBLE);
        rotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setRotation(imageView.getRotation() + 90);
                Log.d(TAG, "onClick: imageview: " + imageView.getRotation());
                Log.d(TAG, "onClick: total: " +totalRotation);
            }
        });
    }

    private void initImgNext() {
        ImageView imageNext = rootView.findViewById(R.id.image_next);
        imageNext.setVisibility(View.VISIBLE);
        imageNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Report currentReport = PostManager.getInstance().getCurrentReport();
                currentReport.setSelectedImage(selectedImage);

                currentReport.setImageRotation(imageView.getRotation() + totalRotation);
                getFragmentManager().popBackStackImmediate();
            }
        });
    }

    private void initCloseImgView() {
        Button close = rootView.findViewById(R.id.back_btn);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.d(TAG, "onClick: close");
                ((PostReportActivity)getActivity()).moveToPostFragment();
            }
        });
    }

    private void initTakePhotoBtn() {
        Button takePhotoBtn = rootView.findViewById(R.id.btn_take_photo);
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startCamera();
                dispatchTakePictureIntent();
            }
        });
    }

    private void initAddImgBtn() {
        Button addImgBtn = rootView.findViewById(R.id.btn_add_image);
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
            }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            progressBar.setVisibility(View.VISIBLE);
            Uri imageUri = null;
            if (requestCode == IMAGE_PICK_CODE && data != null) {
                imageUri = data.getData();
            } else if (requestCode == CAMERA_REQ_CODE) {
                imageUri = Uri.parse(currentPhotoPath);
                checkRotation(imageUri);
            }
            Picasso.get().load(imageUri).fit().into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {

                }
            });
            selectedImage = imageUri.toString();
            initRotateBtn();
            initImgNext();
        }
    }

    //taken from https://developer.android.com/training/camera/photobasics
    private void dispatchTakePictureIntent() {
        if(addImagePermissions.hasPermission(Manifest.permission.CAMERA)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create the File where the photo should go
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.w(TAG, "dispatchTakePictureIntent: ", ex);
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

    private void checkRotation(Uri imgUri) {

        if (imgUri.getScheme().equals("content")) {
            Log.d(TAG, "checkRotation: scheme is content");
            String[] projection = {MediaStore.Images.ImageColumns.ORIENTATION};
            Cursor c = getContext().getContentResolver().query(imgUri, projection, null, null, null);
            if (c.moveToFirst()) {
//                totalRotation = c.getInt(0);
                Log.d(TAG, "checkRotation: totalrotation " + totalRotation);
                c.close();
            }
        } else {
            try {
                InputStream input = getContext().getContentResolver().openInputStream(imgUri);
                ExifInterface ei;
                if (Build.VERSION.SDK_INT > 23) {
                    ei = new ExifInterface(input);
                } else {
                    ei = new ExifInterface(imgUri.getPath());
                }

                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                float rotation;

                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotation = 90;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotation = 180;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotation = 270;
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotation = 0;
                }
                Log.d(TAG, "checkRotation: " + rotation);
                totalRotation = rotation;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "checkRotation: imageView rotation " +imageView.getRotation());
    }
}

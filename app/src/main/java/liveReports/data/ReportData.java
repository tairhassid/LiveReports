package liveReports.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import liveReports.bl.PostManager;
import liveReports.bl.Report;
import liveReports.utils.CallbacksHandler;
import liveReports.utils.Constants;

public class ReportData {

    private static final String TAG = "ReportData";
    private static final String USERS = "users";
    private static final String REPORTS = "reports";
    private static final String GEO_POINT = "geoPoint";
    private static final String IMAGE_DOWNLOAD_URL = "imageDownloadUrl";

    private final String PATH = "photos/posts/";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DocumentReference refToReportsByUser;
    private DocumentReference refToReports;
    private StorageReference storageReference;
    private double mPhotoUploadProgress;


    public ReportData() {
        this.auth = FirebaseAuth.getInstance();
        this.storageReference = FirebaseStorage.getInstance().getReference();
        this.db = FirebaseFirestore.getInstance();
    }

    public void save(Context context, Report report, CallbacksHandler<String> callbacksHandler) {
        saveReport(context, report, callbacksHandler);
    }

    private void saveReport(final Context context, final Report report, final CallbacksHandler<String> callbacksHandler) {
        refToReportsByUser = db.collection(USERS).document(auth.getCurrentUser().getUid()).collection(REPORTS).document();
        refToReports = db.collection(REPORTS).document();
        Log.d(TAG, "saveReport: refToReportsByUser " + refToReportsByUser);
        refToReportsByUser.set(report).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "onComplete: success");
                    refToReports.set(report);
                    if(!TextUtils.isEmpty(report.getSelectedImage())) {
                        Log.d(TAG, "onComplete: not empty");
                        storeImage(context, report, callbacksHandler);
                    } else {
                        Log.d(TAG, "onComplete: callback");
                        callbacksHandler.onCallback(Constants.SUCCESS);
                    }
                }
                else {
                    callbacksHandler.onCallback(Constants.FAIL);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: ");
                callbacksHandler.onCallback(Constants.FAIL);
            }
        });
    }

    private void storeImage(final Context context, Report report, final CallbacksHandler<String> callbacksHandler) {
        final StorageReference photoStorageRef = storageReference.child(PATH + refToReportsByUser.getId());
        try {
            byte[] data = getBytes(context, report);

            UploadTask uploadTask = photoStorageRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    callbacksHandler.onCallback(Constants.FAIL);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onSuccess: upload");
                    photoStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            addPhotoUrlToReport(uri, callbacksHandler);
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100*taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "onProgress: " + progress);
                    if(progress - 10 > mPhotoUploadProgress) {
                        Toast.makeText(context,
                                "Photo upload progress: " + String.format("%.0f", progress),
                                Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] getBytes(Context context, Report report) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                context.getContentResolver(),
                Uri.parse(report.getSelectedImage()));
        Matrix m = new Matrix();
        m.postRotate(report.getImageRotation());
        bitmap = Bitmap.createBitmap(bitmap,0,0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        return baos.toByteArray();
    }

    private void addPhotoUrlToReport(final Uri uri, final CallbacksHandler<String> callbacksHandler) {
        Report currentReport = PostManager.getInstance().getCurrentReport();
        currentReport.setImageDownloadUrl(uri.toString());
        Log.d(TAG, "addPhotoUrlToReport: updating database");

        refToReportsByUser.update(IMAGE_DOWNLOAD_URL, uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                refToReports.update(IMAGE_DOWNLOAD_URL, uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callbacksHandler.onCallback(Constants.SUCCESS);
                    }
                });
            }
        });


    }

    public void getReportsWithinArea(GeoPoint cameraPos, final CallbacksHandler<List<Report>> callbacksHandler) {
        CollectionReference collectionRef = db.collection(REPORTS);

        //inspired by https://stackoverflow.com/questions/55959542/best-way-to-load-markers-from-firestore
        Query markers = collectionRef
                .whereGreaterThanOrEqualTo(
                        GEO_POINT,
                        new GeoPoint(cameraPos.getLatitude() - Constants.DIF,
                                cameraPos.getLongitude() - Constants.DIF))
                .whereLessThanOrEqualTo(
                        GEO_POINT,
                        new GeoPoint(cameraPos.getLatitude() + Constants.DIF,
                                cameraPos.getLongitude() + Constants.DIF));

        markers.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d(TAG, "onComplete: before if");
                if(task.isSuccessful()) {
                    Log.d(TAG, "onComplete: task successful");
                    List<Report> reports = new ArrayList<>();
                    for(DocumentSnapshot document : task.getResult()) {
                        Report report = document.toObject(Report.class);

                        if(!deleteOldReport(report, document)) {
                            Log.d(TAG, "onComplete: " + report);
                            reports.add(report);
                        }
                    }
                    callbacksHandler.onCallback(reports);
                }
            }
        });
    }

    //delete reports that were posted more than 24 hours ago
    private boolean deleteOldReport(Report report, DocumentSnapshot document) {
        Log.d(TAG, "deleteOldReport: report = " + report);
        final Report local = report;
        if(isOlderThanADay(report)) {
            final String imageName = document.getReference().getId();
            document.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess: delete report " + local);
                    if(!TextUtils.isEmpty(local.getImageDownloadUrl())) {
                        deleteImage(imageName);
                    }
                }
            });
            return true;
        }
        return false;
    }

    private void deleteImage(final String imageName) {
        StorageReference imageRef = storageReference.child(PATH + imageName);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: delete image " + imageName);
            }
        });
    }

    private boolean isOlderThanADay(Report report) {
        long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS);
        Date cut = new Date(cutoff);
        Date reportTime = report.getTimestamp();

        if(reportTime != null && reportTime.before(cut)) {
            return true;
        }
        return false;
    }

}


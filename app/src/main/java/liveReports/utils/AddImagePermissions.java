package liveReports.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class AddImagePermissions {

    private Activity callingActivity;
    public static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final int PERMISSIONS_REQ_CODE = 1;


    public AddImagePermissions(Activity callingActivity) {
        this.callingActivity = callingActivity;
    }

    public boolean hasAllPermissions() {
        for(int i=0 ; i<PERMISSIONS.length ; i++) {
            String permissionToCheck = PERMISSIONS[i];
            if(!hasPermission(permissionToCheck)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasPermission(String permissionToCheck) {
        int permissionResult = ActivityCompat.checkSelfPermission(callingActivity, permissionToCheck);

        return permissionResult == PackageManager.PERMISSION_GRANTED;
    }

    public void getAllPermissions() {
        ActivityCompat.requestPermissions(callingActivity, PERMISSIONS, PERMISSIONS_REQ_CODE);
    }

}

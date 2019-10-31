package liveReports.utils;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class FilePathsManager {

    private static final String TAG = "FilePathsManager";
    private static final String APP_NAME = "LiveReports";
    private Activity callingActivity;
    private File rootDir;

    public FilePathsManager(Activity callingActivity) {
        rootDir = new File(callingActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_NAME);

        if(!rootDir.exists() && !rootDir.mkdirs()) {
            Log.d(TAG, "FilePathsManager: failed to create directory");
        }
    }
}

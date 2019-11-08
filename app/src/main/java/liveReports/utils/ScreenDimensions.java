package liveReports.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

//taken from https://github.com/PerrchicK/android-app/tree/170486b92978b39b8f53df389e0cf3324df9c41f
public class ScreenDimensions {

    private DisplayMetrics _metrics;
    private static ScreenDimensions screenDimensions;

    private static ScreenDimensions getInstance() {
        if(screenDimensions == null) {
            screenDimensions = new ScreenDimensions();
        }
        return screenDimensions;
    }

    public static int screenWidthPixels(Context context) {
        return getInstance().getMetrics(context).widthPixels;
    }

    public static int screenHeightPixels(Context context) {
        return getInstance().getMetrics(context).heightPixels;
    }

    private DisplayMetrics getMetrics(Context context) {
        if (_metrics == null) {
            WindowManager wm =(WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
                Display display = wm.getDefaultDisplay();
                _metrics = new DisplayMetrics();
                display.getMetrics(_metrics);
            }
        }
        return _metrics;
    }
}

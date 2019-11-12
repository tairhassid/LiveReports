package liveReports.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import liveReports.bl.Report;

public class Functions {

//    private DisplayMetrics _metrics;

    private static final String TAG = "Functions";
    private static Functions functions;

    private static Functions getInstance() {
        if(functions == null) {
            functions = new Functions();
        }
        return functions;
    }

    public static String parseReportTypeWithEnding(Report report) {
        StringBuilder sb = parseReportType(report);

        sb.append(" Report");
        Log.d(TAG, "parseReportTypeWithEnding: " + sb.toString());
        return sb.toString();
    }

    public static StringBuilder parseReportType(Report report) {
        String typeName = report.getType().name();
        StringBuilder sb = new StringBuilder();
        int current = 0;

        typeName = typeName.replace('_', ' ').toLowerCase();
        Log.d(TAG, "parseReportTypeWithEnding: " + typeName);
        sb.append(typeName.substring(current, current+1).toUpperCase());
        current++;

        int index = 0;
        while(index != -1) {
            index = typeName.indexOf(" ", index);
            Log.d(TAG, "parseReportTypeWithEnding: index=" + index);
            if(index != -1) {
                sb.append(typeName.substring(current, index+1));
                String c = typeName.charAt(index+1)+"";
                sb.append(c.toUpperCase());
                index++;
                current = index+1;
            } else {
                sb.append(typeName.substring(current));
            }
        }
        return sb;
    }

//    public static int screenWidthPixels(Context context) {
//        return getInstance().getMetrics(context).widthPixels;
//    }
//
//    public static int screenHeightPixels(Context context) {
//        return getInstance().getMetrics(context).heightPixels;
//    }
//
//    private DisplayMetrics getMetrics(Context context) {
//        if (_metrics == null) {
//            WindowManager wm =(WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//            if (wm != null) {
//                Display display = wm.getDefaultDisplay();
//                _metrics = new DisplayMetrics();
//                display.getMetrics(_metrics);
//            }
//        }
//        return _metrics;
//    }
}
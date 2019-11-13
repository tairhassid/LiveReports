package liveReports.utils;

import android.util.Log;

import liveReports.bl.Report;

public class Functions {

    private static final String TAG = "Functions";

    //add "Report" ending to the type
    public static String parseReportTypeWithEnding(Report report) {
        StringBuilder sb = parseReportType(report);

        sb.append(" Report");
        Log.d(TAG, "parseReportTypeWithEnding: " + sb.toString());
        return sb.toString();
    }

    // example: SAFETY_HAZARD -> Safety Hazard
    public static StringBuilder parseReportType(Report report) {
        String typeName = report.getType().name();
        StringBuilder sb = new StringBuilder();
        int current = 0;

        typeName = typeName.replace('_', ' ').toLowerCase();
        sb.append(typeName.substring(current, current+1).toUpperCase());
        current++;

        int index = 0;
        while(index != -1) {
            index = typeName.indexOf(" ", index);
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
}
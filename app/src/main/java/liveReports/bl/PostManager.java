package liveReports.bl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import liveReports.data.ReportData;
import liveReports.utils.CallbacksHandler;

public class PostManager {
    private static final String TAG = "PostManager";
    public static PostManager postManager;
    private Report currentReport;
    private ReportData reportData;


    private PostManager() {
        currentReport = new Report();
        reportData = new ReportData();
    }

    public static PostManager getInstance() {
        if(postManager == null) {
            postManager = new PostManager();
        }
        return postManager;
    }

    public Report getCurrentReport() {
        return currentReport;
    }
    
//    public boolean setCurrentReportBeforeSubmit(String name, long typeId, String reportText, LatLng latLng) {
//        if(name.equals("") || (reportText.equals("") && getCurrentReport().getSelectedImage().equals(""))) {
//            Log.d(TAG, "setCurrentReportBeforeSubmit: returning false");
//            return false;
//        }
//        setCurrentReport(name, typeId, reportText, latLng);
//        saveCurrentReportToDatabase();
//        return true;
//    }

    public void setCurrentReport(String name, long typeId, String reportText, GeoPoint geoPoint) {
//        if(!name.equals("") && latLng != null) {
            Log.d(TAG, "setCurrentReport: " + name + " " + reportText);
            currentReport.setName(name);
            currentReport.setType(Report.Type.values()[(int)typeId]);
            currentReport.setReportText(reportText);
            currentReport.setGeoPoint(geoPoint);
//        }
    }

    public boolean saveCurrentReportToDatabase(Context context, CallbacksHandler callbacksHandler) {
        if(currentReport.getName().equals("") ||
                (currentReport.getReportText().equals("") &&
                        currentReport.getSelectedImage().equals(""))) {
            Log.d(TAG, "saveCurrentReportToDatabase: returning false");
            return false;
        }
        reportData.save(context, currentReport, callbacksHandler);
        return true;
    }


    @Override
    public String toString() {
        return "PostManager{" +
                "currentReport=" + currentReport +
                '}';
    }

    public void initNewReport() {
        currentReport = new Report();
    }

}

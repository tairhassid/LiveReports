package liveReports.bl;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
//ignoring extra fields retrieved by a query
public class Report {

    private static final String TAG = "Report";
    private String name;
    private String reportText;
    private @ServerTimestamp Date timestamp;
    //if timestamp == null, firestore automatically insert timestamp
    public enum Type {Weather, SafetyHazard, Fire, Violence, Accident, PublicEvent, Other}
    private Type type;
    private String selectedImage;
    private GeoPoint geoPoint;

    public String getImageDownloadUrl() {
        return imageDownloadUrl;
    }

    public void setImageDownloadUrl(String imageDownloadUrl) {
        this.imageDownloadUrl = imageDownloadUrl;
    }

    private String imageDownloadUrl;


    public Report() {
        name = "";
        reportText = "";
        type = Type.Weather;
        selectedImage = "";
    }

    public Report(String name, String reportText, GeoPoint geoPoint) {
        this.name = name;
        this.reportText = reportText;
        this.geoPoint = geoPoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReportText() {
        return reportText;
    }

    public void setReportText(String reportText) {
        Log.d(TAG, "setReportText: ");
        this.reportText = reportText;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Type getType() {
        return type;
    }

    @Exclude
    public int getOrdinalOfType() {
        Log.d(TAG, "getOrdinalOfType: " + type);
        return this.type.ordinal();
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Exclude
    public String getSelectedImage() {
        return selectedImage;
    }

    public void setSelectedImage(String selectedImage) {
        Log.d(TAG, "setSelectedImage: " + selectedImage);
        this.selectedImage = selectedImage;
    }

    @Override
    public String toString() {
        return "Report{" +
                "name='" + name + '\'' +
                ", reportText='" + reportText + '\'' +
                ", timestamp=" + timestamp +
                ", type=" + type +
                ", geoPoint=" + geoPoint +
                '}';
    }
}

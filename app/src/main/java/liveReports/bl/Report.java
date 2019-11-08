package liveReports.bl;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
//ignoring extra fields retrieved by a query
public class Report implements Parcelable {

    private static final String TAG = "Report";
    private String name;
    private String reportText;
    private @ServerTimestamp Date timestamp;//if timestamp == null,
                                            // firestore automatically insert timestamp
    public enum Type {WEATHER, SAFETY_HAZARD, FIRE, VIOLENCE, ACCIDENT, PUBLIC_EVENT, OTHER}
    private Type type;
    private String selectedImage;
    private String imageDownloadUrl;
    private float imageRotation;
    private GeoPoint geoPoint;


    public Report() {
        name = "";
        reportText = "";
        type = Type.WEATHER;
        selectedImage = "";
        imageDownloadUrl = "";
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

    public String getImageDownloadUrl() {
        return imageDownloadUrl;
    }

    public void setImageDownloadUrl(String imageDownloadUrl) {
        this.imageDownloadUrl = imageDownloadUrl;
    }

    @Exclude
    public float getImageRotation() {
        return imageRotation;
    }

    public void setImageRotation(float imageRotation) {
        this.imageRotation = imageRotation;
    }

    //read
    protected Report(Parcel in) {
        name = in.readString();
        reportText = in.readString();
        timestamp = new Date(in.readLong());
        type = Type.valueOf(in.readString());
        selectedImage = in.readString();
        imageDownloadUrl = in.readString();
        imageRotation = in.readFloat();
        geoPoint = new GeoPoint(in.readDouble(), in.readDouble());
    }

    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(reportText);
        parcel.writeLong(timestamp.getTime());
        parcel.writeString(this.type.name());
        parcel.writeString(selectedImage);
        parcel.writeString(imageDownloadUrl);
        parcel.writeFloat(imageRotation);
        parcel.writeDouble(geoPoint.getLatitude());
        parcel.writeDouble(geoPoint.getLongitude());
    }

    @Override
    public String toString() {
        return "Report{" +
                "name='" + name + '\'' +
                ", reportText='" + reportText + '\'' +
                ", timestamp=" + timestamp +
                ", type=" + type +
                ", selectedImage='" + selectedImage + '\'' +
                ", imageDownloadUrl='" + imageDownloadUrl + '\'' +
                ", geoPoint=" + geoPoint +
                '}';
    }
}

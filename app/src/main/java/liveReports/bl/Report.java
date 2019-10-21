package liveReports.bl;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
//ignoring extra fields retrieved by a query
public class Report {
    private String name;
    private String reportText;
    private @ServerTimestamp Date timestamp;
    //if timestamp == null, firestore automatically insert timestamp
    enum Type {Weather, SafetyHazard, Fire, Violence, Accident, PublicEvent, Other}
    private Type type;
    private LatLng latLng;

    public Report() {
    }

    public Report(String name, String reportText, LatLng latLng) {
        this.name = name;
        this.reportText = reportText;
        this.latLng = latLng;
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
        this.reportText = reportText;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

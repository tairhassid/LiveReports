package liveReports.bl;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import liveReports.data.ReportData;

public class PostReport {
    private ReportData reportData;

    public PostReport() {
        reportData = new ReportData();
    }

    public void saveReport(String name, long typeId, String reportText, LatLng latLng) {
        if(!name.equals("") && !reportText.equals("") && latLng != null) {
            Report report = new Report();

            report.setName(name);
            report.setType(Report.Type.values()[(int)typeId]);
            report.setReportText(reportText);
            report.setLatLng(latLng);
            reportData.save(report);
        }
    }
}

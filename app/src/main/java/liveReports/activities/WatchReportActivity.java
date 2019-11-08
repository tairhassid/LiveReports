package liveReports.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import liveReports.bl.PostManager;
import liveReports.bl.Report;
import liveReports.livereports.R;

public class WatchReportActivity extends AppCompatActivity {

    private static final String TAG = "WatchReportActivity";
    private Button backBtn;
    private Report report;
    private TextView textViewName;
    private TextView textViewTime;
    private TextView textViewReportType;
    private RelativeLayout relativeLayout;
    private TextView textViewReportBody;
    private ImageView imageView;
    //report text
    //image view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_report);

        if(getIntent().hasExtra("report")) {
            report = getIntent().getParcelableExtra("report");
        } else {
            report = PostManager.getInstance().getCurrentReport();
        }
        Log.d(TAG, "onCreate: report = " + report);
        textViewName = findViewById(R.id.text_view_name);
        textViewName.setText(report.getName());

        textViewTime = findViewById(R.id.text_view_time);
        textViewTime.setText(report.getTimestamp().toString());

        textViewReportType = findViewById(R.id.text_view_report_type);
        textViewReportType.setText(parseReportType());

        relativeLayout = findViewById(R.id.rel_layout_watch_report);

        if(!TextUtils.isEmpty(report.getReportText())) {
            createReportBody();
        }
        if(!TextUtils.isEmpty(report.getImageDownloadUrl())) {
            createReportImage();
        }
        initBackButton();
    }

    private String parseReportType() {
        String typeName = report.getType().name();
        StringBuilder sb = new StringBuilder();
        int current = 0;

        typeName = typeName.replace('_', ' ').toLowerCase();
        Log.d(TAG, "parseReportType: " + typeName);
        sb.append(typeName.substring(current, current+1).toUpperCase());
        current++;

        int index = 0;
        while(index != -1) {
            index = typeName.indexOf(" ", index);
            Log.d(TAG, "parseReportType: index=" + index);
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

        sb.append(" Report");
        Log.d(TAG, "parseReportType: " + sb.toString());
        return sb.toString();
    }

    private void createReportImage() {
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 20, 0, 0);

        if(report.getReportText() != null) {
            //the image should be below report's body, the body's id has to be set programmatically
            layoutParams.addRule(RelativeLayout.BELOW, textViewReportBody.getId());
        } else {
            //image below the report type
            layoutParams.addRule(RelativeLayout.BELOW, textViewReportType.getId());
        }
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        View view = LayoutInflater.from(this).inflate(R.layout.snippet_general_image, relativeLayout, false);
        view.setLayoutParams(layoutParams);
        relativeLayout.addView(view);

        ImageView imageView = findViewById(R.id.image_view_preview);
        Picasso.get().load(report.getImageDownloadUrl()).into(imageView);
    }


    private void createReportBody() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.addRule(RelativeLayout.BELOW, textViewReportType.getId());
        layoutParams.setMargins(0, 10, 0, 0);
        textViewReportBody = new TextView(this);
        textViewReportBody.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textViewReportBody.setText(report.getReportText());
        textViewReportBody.setId(R.id.report_body); //created in ids.xml file

        textViewReportBody.setLayoutParams(layoutParams);
        relativeLayout.addView(textViewReportBody);

    }

    private void initBackButton() {
        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WatchReportActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}

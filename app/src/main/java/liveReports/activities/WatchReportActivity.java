package liveReports.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import liveReports.bl.PostManager;
import liveReports.bl.Report;
import liveReports.livereports.R;
import liveReports.utils.Constants;
import liveReports.utils.Functions;

public class WatchReportActivity extends AppCompatActivity {

    private static final String REPORTER = "Reporter";
    private Button backBtn;
    private Report report;
    private TextView textViewName;
    private TextView textViewTime;
    private TextView textViewReportType;
    private RelativeLayout relativeLayout;
    private TextView textViewReportBody;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_report);

        if(getIntent().hasExtra(Constants.REPORT)) {
            report = getIntent().getParcelableExtra(Constants.REPORT);
        } else {
            report = PostManager.getInstance().getCurrentReport();
        }
        initReportDetails();

        relativeLayout = findViewById(R.id.rel_layout_watch_report);

        if(!TextUtils.isEmpty(report.getReportText())) {
            createReportBody();
        }
        if(!TextUtils.isEmpty(report.getImageDownloadUrl())) {
            createReportImage();
        }
        initBackButton();
    }

    private void initReportDetails() {
        textViewName = findViewById(R.id.text_view_name);
        textViewName.setText(new StringBuilder().append(REPORTER).append(" ").append(report.getName()).toString());

        textViewTime = findViewById(R.id.text_view_time);
        textViewTime.setText(report.getTimestamp().toString());

        textViewReportType = findViewById(R.id.text_view_report_type);
        textViewReportType.setText(Functions.parseReportTypeWithEnding(report));
    }

    private void createReportImage() {
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 20, 0, 0);

        if(!TextUtils.isEmpty(report.getReportText())) {
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
        progressBar = findViewById(R.id.progress_bar_image);
        progressBar.setVisibility(View.VISIBLE);

//        imageView.setImageURI(Uri.parse(report.getImageDownloadUrl()));

        Picasso.get().load(report.getImageDownloadUrl()).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(WatchReportActivity.this, "Failed to load photo", Toast.LENGTH_LONG).show();
            }
        });
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
                overridePendingTransition(R.anim.slide_enter_left, R.anim.slide_exit_right);
                finish();
            }
        });
    }
}

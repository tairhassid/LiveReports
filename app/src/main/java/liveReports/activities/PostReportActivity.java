package liveReports.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import liveReports.bl.PostManager;
import liveReports.livereports.R;
import liveReports.ui.AddImageFragment;
import liveReports.ui.PostReportFragment;

public class PostReportActivity extends AppCompatActivity {

    //UI variables
    private Fragment currentFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_report);

        PostManager.getInstance().initNewReport();

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.post_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            PostReportFragment firstFragment = new PostReportFragment();
            currentFragment = firstFragment;

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction().
                    add(R.id.post_container, firstFragment).commit();
        }
    }

    public void moveToAddImageFragment() {
        AddImageFragment addImageFragment = new AddImageFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.post_container, addImageFragment)
                .addToBackStack("post")
                .commit();

        currentFragment = addImageFragment;
    }

    public void moveToPostFragment() {
        PostReportFragment postReportFragment = new PostReportFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.post_container, postReportFragment)
                .commit();

        currentFragment = postReportFragment;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        currentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_enter_diagonal, R.anim.slide_exit_diagonal);
    }
}

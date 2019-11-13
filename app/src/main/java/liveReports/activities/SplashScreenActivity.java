package liveReports.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import liveReports.livereports.R;

public class SplashScreenActivity extends AppCompatActivity {

    //constants
    public static final int ANIM_DURATION = 1000;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //ui variables
    private Button skipBtn;
    private Button signInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        signInBtn = findViewById(R.id.sign_in);
        skipBtn = findViewById(R.id.skip);

        if(currentUser == null) {
            setSignInButton();
            setSkipButton();
            setButtonsAnimation();
        } else {
            setButtonsVisibility();
        }
        setLogoAnimation();
    }

    private void setButtonsVisibility() {
        signInBtn.setVisibility(View.GONE);
        skipBtn.setVisibility(View.GONE);
    }

    private void setButtonsAnimation() {
        RelativeLayout relativeLayout = findViewById(R.id.btns_layout);
        TranslateAnimation translateAnimation =
                new TranslateAnimation(Animation.ABSOLUTE, Animation.ABSOLUTE, Animation.ABSOLUTE, Animation.ABSOLUTE,
                        Animation.RELATIVE_TO_PARENT, 1f, Animation.ABSOLUTE, 1f);
        translateAnimation.setDuration(ANIM_DURATION);
        translateAnimation.setFillAfter(true);

        relativeLayout.startAnimation(translateAnimation);
    }

    private void setLogoAnimation() {
        ImageView logoImageView = findViewById(R.id.logo);

        TranslateAnimation translateAnimation =
                new TranslateAnimation(Animation.ABSOLUTE, Animation.ABSOLUTE, Animation.ABSOLUTE, Animation.ABSOLUTE,
                        Animation.RELATIVE_TO_PARENT, -1f, Animation.ABSOLUTE, 1f);
        translateAnimation.setDuration(ANIM_DURATION);
        translateAnimation.setFillAfter(true);

        logoImageView.startAnimation(translateAnimation);

        if(currentUser != null) {
            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    moveToMainActivity();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void setSignInButton() {

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setSkipButton() {
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToMainActivity();
            }
        });
    }

    private void moveToMainActivity() {
        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}

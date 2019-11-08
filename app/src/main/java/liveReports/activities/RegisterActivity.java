package liveReports.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liveReports.livereports.R;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private FirebaseAuth mAuth;
    private EditText emailText;
    private EditText passwordText;
    private EditText repeatPasswordText;
    private ProgressDialog mProgressDialog;
    private TextView mStatusTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailText = findViewById(R.id.txt_email);
        passwordText = findViewById(R.id.txt_password);
        repeatPasswordText = findViewById(R.id.txt_repeat_password);
        mStatusTextView = findViewById(R.id.status);

//        findViewById(R.id.btn_sign_in).setOnClickListener(this);
        findViewById(R.id.btn_Register).setOnClickListener(this);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        String email = emailText.getText().toString();
        Log.d(TAG, "onClick: " + email);
        String password = passwordText.getText().toString();
        String passwordRepeat = repeatPasswordText.getText().toString();

        createAccount(email, password, passwordRepeat);
    }

    private boolean checkInput(String email, String password, String passwordRepeat) {
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordRepeat)) {
            Log.d(TAG, "checkInput: some input blank");
            mStatusTextView.setText("All fields must be filled out!");
            return false;
        }
        return true;
    }

    private void createAccount(String email, String password, String passwordRepeat) {
        if(!validateForm(email, password, passwordRepeat)) {
            return;
        }
        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
                            moveToMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void moveToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean validateForm(String email, String password, String passwordRepeat) {
        boolean valid = true;
        //taken from https://howtodoinjava.com/regex/java-regex-validate-email-address/
        String emailReg = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
        Pattern pattern = Pattern.compile(emailReg);
        Matcher matcher = pattern.matcher(email);

        if (TextUtils.isEmpty(email)) {
            emailText.setError("Required");
            valid = false;
        } else if(!matcher.matches()) {
            emailText.setError("Email address is not valid");
            valid = false;
        } else {
            emailText.setError(null);
        }


        if (TextUtils.isEmpty(password)) {
            passwordText.setError("Required.");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if(!password.equals(passwordRepeat)) {
            repeatPasswordText.setError("Passwords don't match");
            valid = false;
        } else {
            repeatPasswordText.setError(null);
        }

        return valid;
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}

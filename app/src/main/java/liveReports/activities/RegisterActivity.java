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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

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

        findViewById(R.id.btn_Register).setOnClickListener(this);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String passwordRepeat = repeatPasswordText.getText().toString();

        createAccount(email, password, passwordRepeat);
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
                        if (task.isSuccessful()) { //new user created
                            Log.d(TAG, "createUserWithEmail:success");
                            moveToMainActivity();
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) { //under 6 characters password
                                passwordText.setError(e.getReason());
                                passwordText.requestFocus();
                            } catch(FirebaseAuthUserCollisionException e) { //Email already registered
                                emailText.setError(getString(R.string.error_user_exists));
                                emailText.requestFocus();
                            } catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
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
            emailText.setError(getString(R.string.required));
            valid = false;
        } else if(!matcher.matches()) {
            emailText.setError(getString(R.string.email_not_valid));
            valid = false;
        } else {
            emailText.setError(null);
        }


        if (TextUtils.isEmpty(password)) {
            passwordText.setError(getString(R.string.required));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if(!password.equals(passwordRepeat)) {
            repeatPasswordText.setError(getString(R.string.pass_dont_match));
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
            mProgressDialog.setMessage(getString(R.string.loading));
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}

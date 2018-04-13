package stresstest.ntt.smartband;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import stresstest.ntt.kaist.childcare.MainActivity;
import stresstest.ntt.kaist.childcare.R;
import stresstest.ntt.mymanager.MyFileManager;
import stresstest.ntt.mymanager.MySocketManager;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginSettingActivity extends AppCompatActivity  {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "111:TEST01_FATHER", "222:TEST01_MOTHER",
            "1111:TEST02_FATHER", "2222:TEST02_MOTHER",
            "Momicon5541f:USER01_FATHER", "Momicon5541m:USER01_MOTHER",
            "Momicon4452f:USER02_FATHER", "Momicon4452m:USER02_MOTHER",
            "Momicon9487f:USER03_FATHER", "Momicon9487m:USER03_MOTHER",
            "Momicon1445f:USER04_FATHER", "Momicon1445m:USER04_MOTHER",
            "Momicon6453f:USER05_FATHER", "Momicon6453m:USER05_MOTHER",
            "Momicon9845f:USER06_FATHER", "Momicon9845m:USER06_MOTHER",
            "Momicon1145f:USER07_FATHER", "Momicon1145m:USER07_MOTHER",
            "Momicon1322f:USER08_FATHER", "Momicon1322m:USER08_MOTHER",
            "Momicon8854f:USER09_FATHER", "Momicon8854m:USER09_MOTHER",
            "f1:USER01_FATHER", "m1:USER01_MOTHER",
            "f2:USER02_FATHER", "m2:USER02_MOTHER",
            "f3:USER03_FATHER", "m3:USER03_MOTHER",
            "f4:USER04_FATHER", "m4:USER04_MOTHER",
            "f5:USER05_FATHER", "m5:USER05_MOTHER",
            "f6:USER06_FATHER", "m6:USER06_MOTHER",
            "f7:USER07_FATHER", "m7:USER07_MOTHER",
            "f8:USER08_FATHER", "m8:USER08_MOTHER",
            "f9:USER09_FATHER", "m9:USER09_MOTHER",
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck < 0) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.REORDER_TASKS);
        if(permissionCheck < 0) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.REORDER_TASKS}, 1);
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        MyFileManager myFileManager = new MyFileManager();
        myFileManager.deleteUserInfo();

        // Reset errors.
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email);
            mAuthTask.execute((Void) null);
        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        UserLoginTask(String email) {
            mEmail = email;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    MyFileManager fileManager = new MyFileManager();
                    fileManager.initNewFile(pieces[1]); // Save file

                    if(pieces[0].length()>3) {
                        if (pieces[1].contains("FATHER")) {
                            MySocketManager socketM = new MySocketManager(pieces[1]);
                            socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_FATHER_FCM_TOKEN, "null", 0, FirebaseInstanceId.getInstance().getToken());
                        } else {
                            MySocketManager socketM = new MySocketManager(pieces[1]);
                            socketM.setDataFromServer(MySocketManager.SOCKET_MSG.SET_MOTHER_FCM_TOKEN, "null", 0, FirebaseInstanceId.getInstance().getToken());
                        }
                    }
                    return true;
                }
            }

            // TODO: register the new account here.
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            if (success) {
                Intent intent=new Intent(LoginSettingActivity.this , MainActivity.class);
                startActivity(intent);
                finish();

            } else {
                mEmailView.setError("등록되지 않은 정보입니다.");
                mEmailView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}



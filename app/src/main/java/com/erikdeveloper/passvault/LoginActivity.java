package com.erikdeveloper.passvault;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.erikdeveloper.passvault.couchbase.AndroidCBLStore;
import com.passvault.crypto.AESEngine;
import com.passvault.util.Account;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {


    private static final String TAG = "LoginActivity";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private ArrayList<Account> accounts = new ArrayList<>();

    // UI references.
    private EditText mPasswordView;
    private View mProgressView;
    //private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.erikdeveloper.passvault.R.layout.activity_login);
        // Set up the login form.

        mPasswordView = (EditText) findViewById(com.erikdeveloper.passvault.R.id.login_activity_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == com.erikdeveloper.passvault.R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });


        //mLoginFormView = findViewById(com.developernot.passvault.R.id.login_activity_form);
        mProgressView = findViewById(com.erikdeveloper.passvault.R.id.login_activity_progress);
        Button submitButton = (Button) findViewById(com.erikdeveloper.passvault.R.id.login_activity_sign_in_button);

        submitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
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

        // Reset errors.
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(password);
            mAuthTask.execute((Void) null);
        }
    }


    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 0;
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

            /*mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });*/

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
            //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private String mPassword = "";

        UserLoginTask(String password) {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                int length = Integer.parseInt(prefs.getString(getString(com.erikdeveloper.passvault.R.string.ENCRYPTION_KEY_LENGTH_KEY),
                        getString(com.erikdeveloper.passvault.R.string.DEFAULT_ENCRYPTION_KEY_LENGTH)));
                mPassword = AESEngine.finalizeKey(password, length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                //AndroidCBLStore store = AndroidCBLStore.getInstance("TestKey1TestKey2", LoginActivity.this);
                AndroidCBLStore store = AndroidCBLStore.getInstance(mPassword, LoginActivity.this);
                //Log.e(TAG, "PASSSSSS=" + mPassword);
                store.loadAccounts(accounts);
                //store.resetAccounts(20, accounts, LoginActivity.this);
                Collections.sort(accounts);
                //store.saveAccounts(accounts);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent mainIntent = new Intent();
                Bundle b = new Bundle();
                b.putSerializable(MainActivity.ACCOUNTS_LIST, accounts);
                mainIntent.putExtras(b);
                finish();
                mainIntent.setClass(LoginActivity.this, MainActivity.class);
                startActivity(mainIntent);

            } else {
                AndroidCBLStore.destroyInstance();
                mPasswordView.setError(getString(com.erikdeveloper.passvault.R.string.login_activity_error_incorrect_key));
                mPasswordView.setText("");
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}


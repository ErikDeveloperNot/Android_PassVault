package com.erikdeveloper.passvault;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.erikdeveloper.passvault.couchbase.AndroidCBLStore;
import com.passvault.util.Account;
import com.passvault.util.RandomPasswordGenerator;

import java.util.ArrayList;
import java.util.Set;

public class AddAccountActivity extends Activity {

    private ArrayList<Account> accounts;
    private static final String TAG = "AddAccountActivity";
    //private static final String OVER_RIDE_GEN = "OVER_RIDE_GENERATOR_OPTIONS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.erikdeveloper.passvault.R.layout.activity_add_account);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Always turn off custom password gen options
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean(getString(com.erikdeveloper.passvault.R.string.OVER_GEN_OVERRIDE_KEY), false).commit();

        //// TODO: 5/25/17 pass this in via a bundle
        accounts = MainActivity.getAccounts();

        final EditText accountNameEditText = (EditText) findViewById(com.erikdeveloper.passvault.R.id.add_account_activity_account_name_edittext);
        final EditText accountUserEditText = (EditText) findViewById(com.erikdeveloper.passvault.R.id.add_account_activity_user_name_edittext);
        final EditText accountPassEditText = (EditText) findViewById(com.erikdeveloper.passvault.R.id.add_account_activity_account_password_edittext);
        final EditText accountURLEditTest = (EditText) findViewById(R.id.add_account_activity_url_edittext);
        Button generatePassButton = (Button) findViewById(com.erikdeveloper.passvault.R.id.add_account_activity_generate_password_button);
        Button overridePassButton = (Button) findViewById(com.erikdeveloper.passvault.R.id.add_account_activity_override_default_button);
        Button createAccountButton = (Button) findViewById(com.erikdeveloper.passvault.R.id.add_account_activity_create_button);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accountName = accountNameEditText.getText().toString();
                String accountUser = accountUserEditText.getText().toString();
                String accountPass = accountPassEditText.getText().toString();
                String accountURL = accountURLEditTest.getText().toString();
                String accountUUID = PreferenceManager
                        .getDefaultSharedPreferences(AddAccountActivity.this)
                        .getString("ACCOUNT_UUID_KEY", "");

                if (accountName.length() == 0 || accountUser.length() == 0) {
                    Toast.makeText(AddAccountActivity.this, "Account Name and User Name can't be Blank",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Account account = new Account(accountName, accountUser, accountPass, accountPass,
                        accountUUID, System.currentTimeMillis(), accountURL);

                accounts.add(account);
                AndroidCBLStore.getInstance().saveAccount(account);

                setResult(RESULT_OK);
                finish();
            }
        });

        generatePassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo  Get System default password constraints from somehwere?
                // for now default is 32 length, allows lower, upper, special, digits
                /*
                RandomPasswordGenerator passwdGen = AndroidDefaultRandomPasswordGenerator.getInstance();
                String password = passwdGen.generatePassword();
                accountPassEditText.setText(password);
                */



                RandomPasswordGenerator passwdGen = null;
                String password;
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AddAccountActivity.this);
                boolean overrideGenerator = prefs.getBoolean(getString(com.erikdeveloper.passvault.R.string.OVER_GEN_OVERRIDE_KEY), false);

                boolean lower, upper, digits, special;
                int length;

                if (overrideGenerator) {
                    // get overridden values
                    lower = prefs.getBoolean(getString(com.erikdeveloper.passvault.R.string.OVER_GEN_LOWER_KEY),
                            Boolean.valueOf(getString(com.erikdeveloper.passvault.R.string.DEFAULT_LOWER_GEN)));
                    upper = prefs.getBoolean(getString(com.erikdeveloper.passvault.R.string.OVER_GEN_UPPER_KEY),
                            Boolean.valueOf(getString(com.erikdeveloper.passvault.R.string.DEFAULT_UPPER_GEN)));
                    digits = prefs.getBoolean(getString(com.erikdeveloper.passvault.R.string.OVER_GEN_DIGIT_KEY),
                            Boolean.valueOf(getString(com.erikdeveloper.passvault.R.string.DEFAULT_DIGIT_GEN)));
                    special = prefs.getBoolean(getString(com.erikdeveloper.passvault.R.string.OVER_GEN_SPECIAL_KEY),
                            Boolean.valueOf(getString(com.erikdeveloper.passvault.R.string.DEFAULT_SPECIAL_GEN)));
                    length = Integer.parseInt(prefs.getString(getString(com.erikdeveloper.passvault.R.string.OVER_GEN_LENGTH_KEY),
                            getString(com.erikdeveloper.passvault.R.string.DEFAULT_PASS_LENGTH)));

                    passwdGen = AndroidDefaultRandomPasswordGenerator.getInstance(length, lower, upper, special, digits);

                    if (special) {
                        // get allowed special characters
                        Set<String> set = prefs.getStringSet(getString(com.erikdeveloper.passvault.R.string.OVER_GEN_SPECIAL_SPECIFY_KEY), null);
                        String[] specials = getResources().getStringArray(com.erikdeveloper.passvault.R.array.settings_activity_pass_gen_special_specify_values);
                        //Log.e(TAG, lower+":"+upper+":"+digits+":"+special+":"+length+":"+set+":::"+specials.length);

                        for (String s: specials) {
                            // set should never be null but just in case
                            if (set != null && !set.contains(s)) {
                                //Log.e(TAG, ">>>> " + s);
                                passwdGen.removedAllowedCharacters(s.trim().charAt(0));
                            }
                        }

                    }
                } else {
                    // use generator values
                    lower = prefs.getBoolean(getString(com.erikdeveloper.passvault.R.string.GEN_LOWER_KEY),
                            Boolean.valueOf(getString(com.erikdeveloper.passvault.R.string.DEFAULT_LOWER_GEN)));
                    upper = prefs.getBoolean(getString(com.erikdeveloper.passvault.R.string.GEN_UPPER_KEY),
                            Boolean.valueOf(getString(com.erikdeveloper.passvault.R.string.DEFAULT_UPPER_GEN)));
                    digits = prefs.getBoolean(getString(com.erikdeveloper.passvault.R.string.GEN_DIGIT_KEY),
                            Boolean.valueOf(getString(com.erikdeveloper.passvault.R.string.DEFAULT_DIGIT_GEN)));
                    special = prefs.getBoolean(getString(com.erikdeveloper.passvault.R.string.GEN_SPECIAL_KEY),
                            Boolean.valueOf(getString(com.erikdeveloper.passvault.R.string.DEFAULT_SPECIAL_GEN)));
                    length = Integer.parseInt(prefs.getString(getString(com.erikdeveloper.passvault.R.string.GEN_LENGTH_KEY),
                            getString(com.erikdeveloper.passvault.R.string.DEFAULT_PASS_LENGTH)));

                    passwdGen = AndroidDefaultRandomPasswordGenerator.getInstance(length, lower, upper, special, digits);

                    if (special) {
                        // get allowed special characters
                        Set<String> set = prefs.getStringSet(getString(com.erikdeveloper.passvault.R.string.GEN_SPECIAL_SPECIFY_KEY), null);
                        String[] specials = getResources().getStringArray(com.erikdeveloper.passvault.R.array.settings_activity_pass_gen_special_specify_values);
                        Log.e(TAG, lower + ":" + upper + ":" + digits + ":" + special + ":" + length + ":" + set + ":::" + specials.length);

                        for (String s : specials) {
                            //set maybe null if defaults have not been edited yet
                            if (set != null && !set.contains(s)) {
                                Log.e(TAG, ">>>> " + s);
                                passwdGen.removedAllowedCharacters(s.trim().charAt(0));
                            }
                        }

                    }
                }

                StringBuilder sb = new StringBuilder();
                for (char c: passwdGen.getAllowedCharactres()) {
                    sb.append(c + ",");
                }
                Log.e(TAG, sb.toString());

                password = passwdGen.generatePassword();
                accountPassEditText.setText(password);
            }
        });

        overridePassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent overrideIntent = new Intent(AddAccountActivity.this, OverridePasswordGeneratorOptionsActivity.class);
                startActivity(overrideIntent);
            }
        });

    }


/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, requestCode + ":" + resultCode + ":" + data.toString());
    }
*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

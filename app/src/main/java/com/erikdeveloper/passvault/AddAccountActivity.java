package com.erikdeveloper.passvault;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.passvault.util.Account;
import com.passvault.util.RandomPasswordGenerator;

import java.util.ArrayList;
import java.util.Set;

public class AddAccountActivity extends Activity {

    private ArrayList<Account> accounts;
    private static final String TAG = "AddAccountActivity";
    private RandomPasswordGenerator passwdGen = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.erikdeveloper.passvault.R.layout.activity_add_account);
        getActionBar().setDisplayHomeAsUpEnabled(true);

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

                String accountUUID = AndroidJsonStore.getInstance().loadSettings().getGeneral().getAccountUUID();

                if (accountName.length() == 0 || accountUser.length() == 0) {
                    Toast.makeText(AddAccountActivity.this, "Account Name and User Name can't be Blank",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Account account = new Account(accountName, accountUser, accountPass, accountPass,
                        accountUUID, System.currentTimeMillis(), accountURL);

                if (!accounts.contains(account)) {
                    accounts.add(account);
                    AndroidJsonStore.getInstance().saveAccount(account);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddAccountActivity.this);

                    builder.setMessage("An Account with the same Account Name already exists.")
                            .setTitle("Duplicate Account Name Error");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return;
                }

                setResult(RESULT_OK);
                finish();
            }
        });

        generatePassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password;

                if (passwdGen == null) {
                    passwdGen = new AndroidDefaultRandomPasswordGenerator(
                            AndroidJsonStore.getInstance().loadSettings().getDefaultRandomPasswordGenerator());
                }

                password = passwdGen.generatePassword();
                accountPassEditText.setText(password);
            }
        });

        overridePassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent overrideIntent = new Intent(AddAccountActivity.this, OverridePasswordGeneratorOptionsActivity.class);
                //startActivity(overrideIntent);
                startActivityForResult(overrideIntent, 1);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {// && resultCode == RESULT_OK) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AddAccountActivity.this);

            if (prefs.getBoolean(getString(com.erikdeveloper.passvault.R.string.OVER_GEN_OVERRIDE_KEY), false)) {
                boolean lower = prefs.getBoolean(getString(R.string.OVER_GEN_LOWER_KEY), true);
                boolean upper = prefs.getBoolean(getString(R.string.OVER_GEN_UPPER_KEY), true);
                boolean digits = prefs.getBoolean(getString(R.string.OVER_GEN_DIGIT_KEY), true);
                int length = Integer.parseInt(prefs.getString(getString(R.string.OVER_GEN_LENGTH_KEY), "32"));
                Set<String> specials = null;

                if (prefs.getBoolean(getString(R.string.OVER_GEN_SPECIAL_KEY), false)) {
                    // get allowed special characters
                    specials = prefs.getStringSet(getString(R.string.OVER_GEN_SPECIAL_SPECIFY_KEY), null);
                }

                passwdGen = AndroidDefaultRandomPasswordGenerator.getInstance(
                                    length,
                                    lower,
                                    upper,
                                    ((specials == null) ? true : !specials.isEmpty()),
                                    digits);

                if (specials != null && !specials.isEmpty()) {
                    String[] defaultSpecials =
                            getResources().getStringArray(com.erikdeveloper.passvault.R.array.settings_activity_pass_gen_special_specify_values);

                    for (String s : defaultSpecials) {
                        if (!specials.contains(s)) {
                            Log.e(TAG, "Removing Special Character: " + s);
                            passwdGen.removedAllowedCharacters(s.trim().charAt(0));
                        }
                    }
                }
            }
        } else {
            passwdGen = null;
        }

    }


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

package com.erikdeveloper.passvault;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.passvault.util.Account;
import com.passvault.util.RandomPasswordGenerator;

import java.util.Set;

public class EditAccountActivity extends Activity {

    private RandomPasswordGenerator passwdGen = null;
    private Account account = null;
    private static final String TAG = "EditAccountActivity";

    public static final String EDIT_ACCOUNT = "ACCOUNT_TO_EDIT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.erikdeveloper.passvault.R.layout.activity_edit_account);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText accountNameEditText = (EditText) findViewById(com.erikdeveloper.passvault.R.id.add_account_activity_account_name_edittext);
        final EditText accountUserEditText = (EditText) findViewById(com.erikdeveloper.passvault.R.id.add_account_activity_user_name_edittext);
        final EditText accountPassEditText = (EditText) findViewById(com.erikdeveloper.passvault.R.id.add_account_activity_account_password_edittext);
        final EditText accountURLEditText = (EditText) findViewById(R.id.add_account_activity_url_edittext);
        Button generatePassButton = (Button) findViewById(com.erikdeveloper.passvault.R.id.add_account_activity_generate_password_button);
        Button overridePassButton = (Button) findViewById(com.erikdeveloper.passvault.R.id.add_account_activity_override_default_button);
        Button updateAccountButton = (Button) findViewById(com.erikdeveloper.passvault.R.id.update_account_activity_update_button);

        Bundle b = this.getIntent().getExtras();
        if (b != null) {
            account = (Account) b.getSerializable(EDIT_ACCOUNT);
            //MainActivity.getAccounts().remove(account);
        }

        accountNameEditText.setText(account.getName());
        accountUserEditText.setText(account.getUser());
        accountPassEditText.setText(account.getPass());

        if (!account.getUrl().equalsIgnoreCase("http://"))
            accountURLEditText.setText(account.getUrl());

        updateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (account.getName().length() == 0 || account.getUser().length() == 0) {
                    Toast.makeText(EditAccountActivity.this, "Account Name and User Name can't be Blank",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (!accountPassEditText.getText().toString().equalsIgnoreCase(account.getOldPass()))
                    account.setOldPass(account.getPass());

                account.setName(accountNameEditText.getText().toString());
                account.setUser(accountUserEditText.getText().toString());
                account.setPass(accountPassEditText.getText().toString());
                account.setUrl(accountURLEditText.getText().toString());

                AndroidJsonStore.getInstance().saveAccount(account);
                MainActivity.getAccounts().remove(account);
                MainActivity.getAccounts().add(account);

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
                Intent overrideIntent = new Intent(EditAccountActivity.this, OverridePasswordGeneratorOptionsActivity.class);
                //startActivity(overrideIntent);
                startActivityForResult(overrideIntent, 1);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {// && resultCode == RESULT_OK) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(EditAccountActivity.this);

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
System.out.println("special null=" + (specials != null) + ", empty=" + !specials.isEmpty() + ", getB=" + prefs.getBoolean(getString(R.string.OVER_GEN_SPECIAL_KEY), false));
                if (specials != null && !specials.isEmpty() && prefs.getBoolean(getString(R.string.OVER_GEN_SPECIAL_KEY), false)) {
                    String[] defaultSpecials =
                            getResources().getStringArray(com.erikdeveloper.passvault.R.array.settings_activity_pass_gen_special_specify_values);

                    for (String s : defaultSpecials) {
                        if (!specials.contains(s)) {
                            Log.e(TAG, "Removing Special Character: " + s);
                            passwdGen.removedAllowedCharacters(s.trim().charAt(0));
                        }
                    }
                }

                if (passwdGen.getAllowedCharactres().isEmpty())
                    passwdGen = null;
            }
        } else {
            passwdGen = null;
        }

    }
}

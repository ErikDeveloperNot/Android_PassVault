package com.developernot.passvault;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.developernot.passvault.couchbase.AndroidCBLStore;
import com.passvault.crypto.AESEngine;

import java.util.Arrays;
import java.util.TreeSet;

public class SettingsActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsActivity";

    //// TODO: 5/21/17  - check how to make widgets update with new value when manually set it with editor.commit
    //                    check better way to provide contraints then using OnSharedPreferenceChangeListener


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                .beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);


//  We could have condensed the 5 lines into 1 line of code.
//		getFragmentManager().beginTransaction()
//				.replace(android.R.id.content, new PrefsFragment()).commit();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //use to validate certain preferences, Invalid data will show a dialog & revert to default
        //for now this is a hack
        Log.e(TAG, key);


        if (key.equalsIgnoreCase(getString(com.developernot.passvault.R.string.CLIP_NUMBER_REMOVE_KEY))) {
            Log.e(TAG, key);
            int test;

            /*String value = sharedPreferences.getString(getString(R.string.CLIP_NUMBER_REMOVE_KEY), "20");
            Log.e(TAG, value);*/
            try {
                test = Integer.parseInt(sharedPreferences.getString(key, "20").trim());
            } catch (NumberFormatException e) {
                sharedPreferences.edit().putString(key, "20").commit();
                showAlertDialogIntentFailed("ERROR", getString(com.developernot.passvault.R.string.settings_activity_clipboard_delete_error));
                test = 20;
            }

            // dont allow values over 25
            if (test > 25)
                sharedPreferences.edit().putString(key, "20").commit();

        } else if (key.equalsIgnoreCase(getString(com.developernot.passvault.R.string.GEN_OVERRIDE_KEY))) {
            boolean on = sharedPreferences.getBoolean(key, Boolean.valueOf(getString(com.developernot.passvault.R.string.DEFAULT_OVERRIDE_GEN)));
            Log.e(TAG, ">> on=" + on);
            if (!on) {
                //make sure to reset to defaults
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putBoolean(getString(com.developernot.passvault.R.string.GEN_LOWER_KEY),
                        Boolean.valueOf(getString(com.developernot.passvault.R.string.DEFAULT_LOWER_GEN)));
                edit.putBoolean(getString(com.developernot.passvault.R.string.GEN_UPPER_KEY),
                        Boolean.valueOf(getString(com.developernot.passvault.R.string.DEFAULT_UPPER_GEN)));
                edit.putBoolean(getString(com.developernot.passvault.R.string.GEN_DIGIT_KEY),
                        Boolean.valueOf(getString(com.developernot.passvault.R.string.DEFAULT_DIGIT_GEN)));
                edit.putBoolean(getString(com.developernot.passvault.R.string.GEN_SPECIAL_KEY),
                        Boolean.valueOf(getString(com.developernot.passvault.R.string.DEFAULT_SPECIAL_GEN)));
                edit.putString(getString(com.developernot.passvault.R.string.GEN_LENGTH_KEY),
                        getString(com.developernot.passvault.R.string.DEFAULT_PASS_LENGTH));
                edit.putStringSet(getString(com.developernot.passvault.R.string.GEN_SPECIAL_SPECIFY_KEY),
                        new TreeSet<String>(Arrays.asList(getResources().
                                getStringArray(com.developernot.passvault.R.array.settings_activity_pass_gen_special_specify_values))));
                edit.commit();
            }
        } else if (key.equalsIgnoreCase(getString(com.developernot.passvault.R.string.ENCRYPTION_KEY_LENGTH_KEY))) {
            String currentKey = AndroidCBLStore.getInstance().getEncryptionKey();
            int keyLength = Integer.parseInt(sharedPreferences.getString(getString(com.developernot.passvault.R.string.ENCRYPTION_KEY_LENGTH_KEY),
                    getString(com.developernot.passvault.R.string.DEFAULT_ENCRYPTION_KEY_LENGTH)));
            
            if (currentKey.length() != keyLength) {
                //new keylength doesnt match old, finalize are encrypt accounts 
                try {
                    String newKey = AESEngine.finalizeKey(currentKey, keyLength);
                    Log.e(TAG, "finalKey=" + newKey);
                    AndroidCBLStore.getInstance().setEncryptionKey(newKey);
                    AndroidCBLStore.getInstance().saveAccounts(MainActivity.getAccounts());
                } catch (Exception e) {
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString(getString(com.developernot.passvault.R.string.ENCRYPTION_KEY_LENGTH_KEY), String.valueOf(currentKey.length()));
                    edit.commit();
                    e.printStackTrace();
                    showAlertDialogIntentFailed("ERROR", "Failed to change Encryption key size");
                }
            }
        }

    }


    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(com.developernot.passvault.R.xml.preferences);
        }
    }


    private void showAlertDialogIntentFailed(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message)
                .setTitle(title);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

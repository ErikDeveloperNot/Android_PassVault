package com.developernot.passvault;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class OverridePasswordGeneratorOptionsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: 5/25/17 revist if there is a good way to make this behave like back button
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setTitle("Return to Add Account");
        //getActionBar().setDisplayShowHomeEnabled(true);
        //setContentView(R.layout.activity_password_generator);

        // Display the fragment as the main content.
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                .beginTransaction();
        OverridePasswordGeneratorOptionsActivity.PrefsFragment mPrefsFragment = new OverridePasswordGeneratorOptionsActivity.PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();

    }


    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(com.developernot.passvault.R.xml.over_ride_password_gen);
        }
    }
}

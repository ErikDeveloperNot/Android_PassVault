package com.erikdeveloper.passvault;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.erikdeveloper.passvault.couchbase.AndroidCBLStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.passvault.util.model.Gateway;
import com.passvault.util.model.Gateways;
import com.passvault.util.register.RegisterAccount;
import com.passvault.util.register.RegisterResponse;

public class SyncActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private static final String TAG = "SyncActivity";

    public static enum GatewayType {Remote, Local}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
    /**
     * A placeholder fragment containing a simple view.
     *
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        /*private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }*/

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         *
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sync, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            return rootView;
        }
    }*/


    public static class FreeServiceFragment extends Fragment {

        private static final String ARG_EMAIL = "email";
        private static final String ARG_PASSWORD = "pass";
        private ComponentHolder gui;

        class ComponentHolder {
            EditText email, password;
            Button leftButton, rightButton;
            TextView dialog;
        }

        public FreeServiceFragment() {
            gui = new ComponentHolder();
        }

        public static FreeServiceFragment newInstance(SharedPreferences prefs) {
            FreeServiceFragment fragment = new FreeServiceFragment();
            Gateway gateway = getGateway(prefs, fragment.getContext(), GatewayType.Remote);
            Bundle args = new Bundle();

            if (gateway != null) {
                args.putString(ARG_EMAIL, gateway.getUserName());
                args.putString(ARG_PASSWORD, gateway.getPassword());
            }

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            final String email = getArguments().getString(ARG_EMAIL);
            String password = getArguments().getString(ARG_PASSWORD);
            final String registerServer = getString(R.string.sync_free_registration_server);

            View rootView = inflater.inflate(R.layout.fragment_sync_free, container, false);
            TextView dialogView = (TextView) rootView.findViewById(R.id.fragment_sync_free_dialog_textview);
            final EditText emailView = (EditText) rootView.findViewById(R.id.fragment_sync_free_email_edittext);
            final EditText passwordView = (EditText) rootView.findViewById(R.id.fragment_sync_free_password_edittext);
            Button leftButton = (Button) rootView.findViewById(R.id.fragment_sync_free_left_button);
            Button rightButton = (Button) rootView.findViewById(R.id.fragment_sync_free_right_button);

            gui.email = emailView;
            gui.password = passwordView;
            gui.leftButton = leftButton;
            gui.rightButton = rightButton;
            gui.dialog = dialogView;

            if ( email != null && !email.equalsIgnoreCase("")) {
                dialogView.setText(R.string.fragment_sync_free_existing_account_dialog);
                emailView.setText(email);
                emailView.setFocusable(false);
                passwordView.setText(password);
                passwordView.setFocusable(false);

                leftButton.setText(getString(R.string.fragment_sync_free_existing_account_left_button));
                leftButton.setOnClickListener(createDeleteOnClickListener(registerServer, email, password, gui));
                rightButton.setText(getString(R.string.fragment_sync_free_existing_account_right_button));
                rightButton.setOnClickListener(createRemoveOnClickListener(registerServer, gui));

            } else {
                dialogView.setText(R.string.fragment_sync_free_no_account_dialog);
                leftButton.setText(getString(R.string.fragment_sync_free_no_account_left_button));
                leftButton.setOnClickListener(createCreateOnClickListener(registerServer, gui));
                rightButton.setText(getString(R.string.fragment_sync_free_no_account_right_button));
                rightButton.setOnClickListener(createConfigureOnClickListener(registerServer, gui));
            }

            return rootView;
        }


        /*
         * OnClick Listeners
         */
        private View.OnClickListener createDeleteOnClickListener(final String registerServer, final String email,
                                                                 final String password, final ComponentHolder gui) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final RegisterAccount register = new RegisterAccount(registerServer);

                    class SendDelete extends AsyncTask<String, Void, RegisterResponse> {
                        @Override
                        protected RegisterResponse doInBackground(String... credentials) {
                            return register.deleteAccount(credentials[0], credentials[1]);
                        }

                        @Override
                        protected void onPostExecute(RegisterResponse registerResponse) {
                            super.onPostExecute(registerResponse);
// TODO - currently update accountUUID no matter what, if a network error happens shouldn't do this
                            if (registerResponse.success()) {
                                Toast.makeText(getContext(), "Success: " + registerResponse.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "Error: " + registerResponse.getError(),
                                        Toast.LENGTH_LONG).show();
                            }

                            setComponentsNoAccount(gui, registerServer);

                        }
                    }

                    // send delete to registration server
                    new SendDelete().execute(email, password);
                    // update CBL documents with no accountUUID, **Note no reason to reload UI accounts**
                    AndroidCBLStore.getInstance().updateAccountUUID(MainActivity.getAccounts(), "");
                    // remove gateway from config
                    saveGateway(null, PreferenceManager.getDefaultSharedPreferences(getContext()),
                            getContext(), GatewayType.Remote);
                }
            };
        }

        private View.OnClickListener createRemoveOnClickListener(final String registerServer, final ComponentHolder gui) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // update CBL documents with no accountUUID, **Note no reason to reload UI accounts**
                    AndroidCBLStore.getInstance().updateAccountUUID(MainActivity.getAccounts(), "");
                    // remove gateway from config
                    saveGateway(null, PreferenceManager.getDefaultSharedPreferences(getContext()),
                            getContext(), GatewayType.Remote);

                    setComponentsNoAccount(gui, registerServer);

                }
            };
        }

        private View.OnClickListener createCreateOnClickListener(final String registerServer, final ComponentHolder gui) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final RegisterAccount register = new RegisterAccount(registerServer);
                    final String email = gui.email.getText().toString().trim();
                    final String password = gui.password.getText().toString().trim();

                    if ((email == null || email.equalsIgnoreCase("")) || (password == null || password.equalsIgnoreCase(""))) {
                        Toast.makeText(getContext(), "Email/Passowrd need to be supplied", Toast.LENGTH_LONG).show();
                        return;
                    }

                    class SendCreate extends AsyncTask<String, Void, RegisterResponse> {
                        @Override
                        protected RegisterResponse doInBackground(String... credentials) {
                            return register.registerV1(credentials[0], credentials[1]);
                        }

                        @Override
                        protected void onPostExecute(RegisterResponse registerResponse) {
                            super.onPostExecute(registerResponse);

                            if (registerResponse.success()) {
                                Toast.makeText(getContext(), "Success: " + registerResponse.getMessage(),
                                        Toast.LENGTH_LONG).show();

                                // update CBL documents with new accountUUID, **Note no reason to reload UI accounts**
                                AndroidCBLStore.getInstance().updateAccountUUID(MainActivity.getAccounts(), email);
                                // create the remote gateway config
                                Object gateway = registerResponse.getReturnValue();

                                if (gateway != null && gateway instanceof  Gateway) {
                                    saveGateway((Gateway)gateway, PreferenceManager.getDefaultSharedPreferences(getContext()),
                                            getContext(), GatewayType.Remote);
                                } else {
                                    Toast.makeText(getContext(), "Gateway configuration not returned: " + registerResponse.getError(),
                                            Toast.LENGTH_LONG).show();
                                }

                                setComponentsAccountExists(gui, email, password, registerServer);

                            } else {
                                Toast.makeText(getContext(), "Error: " + registerResponse.getError(),
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                    }

                    new SendCreate().execute(email, password);
                }
            };
        }

        private View.OnClickListener createConfigureOnClickListener(final String registerServer, final ComponentHolder gui) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final RegisterAccount register = new RegisterAccount(registerServer);
                    final String email = gui.email.getText().toString().trim();
                    final String password = gui.password.getText().toString().trim();

                    if ((email == null || email.equalsIgnoreCase("")) || (password == null || password.equalsIgnoreCase(""))) {
                        Toast.makeText(getContext(), "Email/Passowrd need to be supplied", Toast.LENGTH_LONG).show();
                        return;
                    }

                    class SendGetConfig extends AsyncTask<Void, Void, RegisterResponse> {
                        @Override
                        protected RegisterResponse doInBackground(Void... voids) {
                            return register.getGatewatConfig();
                        }

                        @Override
                        protected void onPostExecute(RegisterResponse registerResponse) {
                            super.onPostExecute(registerResponse);

                            if (registerResponse.success()) {
                                Toast.makeText(getContext(), "Gateway configuration returned", Toast.LENGTH_LONG).show();

                                // update CBL documents with new accountUUID, **Note no reason to reload UI accounts**
                                AndroidCBLStore.getInstance().updateAccountUUID(MainActivity.getAccounts(), email);
                                // create the remote gateway config
                                Object gateway = registerResponse.getReturnValue();

                                if (gateway != null && gateway instanceof  Gateway) {
                                    ((Gateway)gateway).setUserName(email);
                                    ((Gateway)gateway).setPassword(password);
                                    saveGateway((Gateway)gateway, PreferenceManager.getDefaultSharedPreferences(getContext()),
                                            getContext(), GatewayType.Remote);
                                } else {
                                    Toast.makeText(getContext(), "Gateway configuration not returned: " + registerResponse.getError(),
                                            Toast.LENGTH_LONG).show();
                                }

                                setComponentsAccountExists(gui, email, password, registerServer);

                            } else {
                                Toast.makeText(getContext(), "Error: " + registerResponse.getError(),
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                    }

                    new SendGetConfig().execute();
                }
            };
        }
        /*
         * End OnClick Listeners
         */


        private void setComponentsAccountExists(ComponentHolder gui, String email, String password, String registerServer) {
            gui.email.setText(email);
            gui.password.setText(password);
            gui.email.setFocusable(false);
            gui.password.setFocusable(false);
            gui.leftButton.setOnClickListener(createDeleteOnClickListener(registerServer, email, password, gui));
            gui.rightButton.setOnClickListener(createRemoveOnClickListener(registerServer, gui));
            gui.dialog.setText(R.string.fragment_sync_free_existing_account_dialog);
            gui.leftButton.setText(R.string.fragment_sync_free_existing_account_left_button);
            gui.rightButton.setText(R.string.fragment_sync_free_existing_account_right_button);
        }


        private void setComponentsNoAccount(ComponentHolder gui, String registerServer) {
            gui.email.setText("");
            gui.password.setText("");
            gui.email.setFocusable(true);
            gui.email.setFocusableInTouchMode(true);
            gui.password.setFocusable(true);
            gui.password.setFocusableInTouchMode(true);
            gui.dialog.setText(R.string.fragment_sync_free_no_account_dialog);
            gui.leftButton.setOnClickListener(createCreateOnClickListener(registerServer, gui));
            gui.leftButton.setText(R.string.fragment_sync_free_no_account_left_button);
            gui.rightButton.setOnClickListener(createConfigureOnClickListener(registerServer, gui));
            gui.rightButton.setText(R.string.fragment_sync_free_no_account_right_button);
        }

    }



    public static class PersonalServiceFragment extends Fragment {

        private static final String ARG_SERVER = "server";
        private static final String ARG_PROTOCOL = "protocol";
        private static final String ARG_PORT = "port";
        private static final String ARG_DB = "db";
        private static final String ARG_USER = "user";
        private static final String ARG_PASSWORD = "pass";

        private SharedPreferences prefs;

        public PersonalServiceFragment() {

        }

        public static PersonalServiceFragment newInstance(SharedPreferences prefs) {
            PersonalServiceFragment fragment = new PersonalServiceFragment();
            fragment.prefs = prefs;
            Gateway gateway = getGateway(prefs, fragment.getContext(), GatewayType.Local);
            Bundle args = new Bundle();

            if (gateway != null) {
                args.putString(ARG_SERVER, gateway.getServer());
                args.putString(ARG_PROTOCOL, gateway.getProtocol());
                args.putInt(ARG_PORT, gateway.getPort());
                args.putString(ARG_DB, gateway.getBucket());
                args.putString(ARG_USER, gateway.getUserName());
                args.putString(ARG_PASSWORD, gateway.getPassword());
            }

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Bundle args = getArguments();

            //final String email = getArguments().getString(ARG_EMAIL);
            String password = getArguments().getString(ARG_PASSWORD);


            View rootView = inflater.inflate(R.layout.fragment_sync_personal, container, false);
            TextView dialogView = (TextView) rootView.findViewById(R.id.fragment_sync_personal_dialog_textview);
            final EditText serverView = (EditText) rootView.findViewById(R.id.fragment_sync_personal_server_edittext);
            final EditText protocolView = (EditText) rootView.findViewById(R.id.fragment_sync_personal_protocol_edittext);
            final EditText portView = (EditText) rootView.findViewById(R.id.fragment_sync_personal_port_edittext);
            final EditText dbView = (EditText) rootView.findViewById(R.id.fragment_sync_personal_database_edittext);
            final EditText userView = (EditText) rootView.findViewById(R.id.fragment_sync_personal_username_edittext);
            final EditText passView = (EditText) rootView.findViewById(R.id.fragment_sync_personal_password_edittext);

            dialogView.setText(R.string.fragment_sync_personal_dialog);
            serverView.setText(args.getString(ARG_SERVER, ""));
            protocolView.setText(args.getString(ARG_PROTOCOL, ""));
            dbView.setText(args.getString(ARG_DB, ""));
            userView.setText(args.getString(ARG_USER, ""));
            passView.setText(args.getString(ARG_PASSWORD, ""));

           if (args.getInt(ARG_PORT, -1) > 0)
                portView.setText(String.valueOf(args.getInt(ARG_PORT)));
            else
                portView.setText("");

            Button deleteButton = (Button) rootView.findViewById(R.id.fragment_sync_personal_left_button);
            Button saveButton = (Button) rootView.findViewById(R.id.fragment_sync_personal_right_button);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    serverView.setText("");
                    protocolView.setText("");
                    dbView.setText("");
                    userView.setText("");
                    passView.setText("");
                    portView.setText("");

                    if (!saveGateway(null, prefs, getContext(), GatewayType.Local)) {
                        Log.e(TAG, "Failed to delete gateway");
                    }
                }
            });

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Gateway gateway = new Gateway();
                    gateway.setServer(serverView.getText().toString().trim());
                    gateway.setProtocol(protocolView.getText().toString().trim());
                    gateway.setBucket(dbView.getText().toString().trim());
                    gateway.setUserName(userView.getText().toString().trim());
                    gateway.setPassword(passView.getText().toString().trim());

                    try {
                        gateway.setPort(Integer.parseInt(portView.getText().toString().trim()));
                    } catch (Exception e) {e.printStackTrace();}

                    if (saveGateway(gateway, prefs, getContext(), GatewayType.Local)) {
                        Toast.makeText(getContext(), "Gateway configuration saved", Toast.LENGTH_LONG).show();
                    }
                }
            });

            return rootView;
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SyncActivity.this);

            if (position == 0)
                return FreeServiceFragment.newInstance(prefs);
            else
                return PersonalServiceFragment.newInstance(prefs);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "FREE SYNC SERVICE";
                case 1:
                    return "PERSONAL SYNC SERVER";

            }
            return null;
        }
    }


    public static Gateway getGateway(SharedPreferences prefs, Context context, GatewayType type) {
        String json = prefs.getString(MainActivity.GATEWAYS, null);
        ObjectMapper mapper = new ObjectMapper();
        Gateway toReturn = null;

        if (json != null) {

            try {
                Gateways gateways = mapper.readValue(json, Gateways.class);

                switch (type) {
                    case Remote:
                        toReturn = gateways.getRemote();
                        break;
                    case Local:
                        // unlike the desktop version only allow one local GW config
                        if (gateways.getLocal() != null)
                            toReturn = gateways.getLocal()[0];
                        break;
                }

                Log.e(TAG, "Returning gateway: " + toReturn);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Error trying retrieve gateway: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                //Log.e(TAG, "Error trying retrieve gateway: " + e.getMessage());
            }
        }

        return toReturn;
    }


    public static boolean saveGateway(Gateway gateway, SharedPreferences prefs, Context context, GatewayType type) {
        // verify gateway has the minimum required bits
        boolean saveGateway = true;
        String server = null;
        String db = null;
        String protocol = null;
        int port = -1;

        if (gateway != null) {
            server = gateway.getServer();
            db = gateway.getBucket();
            protocol = gateway.getProtocol();
            port = gateway.getPort();

            if (server == null || server.equalsIgnoreCase(""))
                saveGateway = false;

            if (db == null || db.equalsIgnoreCase(""))
                saveGateway = false;

            if (protocol == null || protocol.equalsIgnoreCase(""))
                saveGateway = false;

            if (port < 1)
                saveGateway = false;

        } else {
            // Allow null gateway to be saved in order to delete existing gateway
            //saveGateway = false;
        }

        if (!saveGateway) {
            Toast.makeText(context, "Gateway not configured correctly:\n" +
                    "\nServer: " + server +
                    "\nProtocol: " + protocol +
                    "\nPort: " + port +
                    "\nDatabase: " + db,
                    Toast.LENGTH_LONG).show();

            return false;
        }

        String json = prefs.getString(MainActivity.GATEWAYS, null);
        ObjectMapper mapper = new ObjectMapper();
        Gateways gateways = null;

        if (json != null) {

            try {
                gateways = mapper.readValue(json, Gateways.class);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Error trying retrieve gateway: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                gateways = new Gateways();
            }
        } else {
            gateways = new Gateways();
        }

        switch (type) {
            case Remote:
                gateways.setRemote(gateway);
                break;
            case Local:
                // unlike the desktop version only allow one local GW config
                gateways.setLocal(new Gateway[] {gateway});
                break;
        }

        try {
            prefs.edit().putString(MainActivity.GATEWAYS, mapper.writeValueAsString(gateways)).commit();
            Log.e(TAG, "Saved gateway: " + gateway);
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error saving gateway: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        return false;
    }


}

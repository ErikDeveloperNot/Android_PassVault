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

    //public static enum GatewayType {Remote, Local}
    public static enum GatewayType {Remote}



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if no gateway has ever be used setup an empty one
        Gateways gateways = AndroidJsonStore.getInstance().loadSettings().getSync();

        System.out.println("GATEWAYS=" + gateways);
        if (gateways == null) {
            gateways = new Gateways();
            gateways.setRemote(new Gateway());
            AndroidJsonStore.getInstance().loadSettings().setSync(gateways);
            AndroidJsonStore.getInstance().saveSettings(AndroidJsonStore.getInstance().loadSettings());
        }

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
                    final RegisterAccount register = new RegisterAccount(registerServer, RegisterAccount.StoreType.JSON);

                    class SendDelete extends AsyncTask<String, Void, RegisterResponse> {
                        @Override
                        protected RegisterResponse doInBackground(String... credentials) {
                            return register.deleteAccount(credentials[0], credentials[1]);
                        }

                        @Override
                        protected void onPostExecute(RegisterResponse registerResponse) {
                            super.onPostExecute(registerResponse);

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
                    AndroidJsonStore.getInstance().updateAccountUUID(MainActivity.getAccounts(), "");
                    // remove gateway from config
                    saveGateway(null, getContext(), GatewayType.Remote);
                }
            };
        }

        private View.OnClickListener createRemoveOnClickListener(final String registerServer, final ComponentHolder gui) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AndroidJsonStore.getInstance().updateAccountUUID(MainActivity.getAccounts(), "");
                    // remove gateway from config
                    saveGateway(null, getContext(), GatewayType.Remote);

                    setComponentsNoAccount(gui, registerServer);

                }
            };
        }

        private View.OnClickListener createCreateOnClickListener(final String registerServer, final ComponentHolder gui) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final RegisterAccount register = new RegisterAccount(registerServer, RegisterAccount.StoreType.JSON);
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
                                AndroidJsonStore.getInstance().updateAccountUUID(MainActivity.getAccounts(), email);
                                // create the remote gateway config
                                Object gateway = registerResponse.getReturnValue();

                                if (gateway != null && gateway instanceof  Gateway) {
                                    saveGateway((Gateway)gateway, getContext(), GatewayType.Remote);
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
                    final RegisterAccount register = new RegisterAccount(registerServer, RegisterAccount.StoreType.JSON);
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
                                AndroidJsonStore.getInstance().updateAccountUUID(MainActivity.getAccounts(), email);
                                // create the remote gateway config
                                Object gateway = registerResponse.getReturnValue();

                                if (gateway != null && gateway instanceof  Gateway) {
                                    ((Gateway)gateway).setUserName(email);
                                    ((Gateway)gateway).setPassword(password);
                                    saveGateway((Gateway)gateway, getContext(), GatewayType.Remote);
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

            if (position == 0) {
                return FreeServiceFragment.newInstance(prefs);
            } else {
                Log.w(TAG, "Error returning Fragment, position id set to: " + position);
                return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            //return 2;
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "FREE SYNC SERVICE";
                /*case 1:
                    return "PERSONAL SYNC SERVER";*/

            }
            return null;
        }
    }


    public static Gateway getGateway(SharedPreferences prefs, Context context, GatewayType type) {

        Gateway toReturn = null;
        Gateways gateways = AndroidJsonStore.getInstance().loadSettings().getSync();

        switch (type) {
            case Remote:
                toReturn = gateways.getRemote();
                break;
            default:
                toReturn = gateways.getRemote();
        }

        Log.e(TAG, "Returning gateway: " + toReturn);

        return toReturn;
    }


    public static boolean saveGateway(Gateway gateway, /*SharedPreferences prefs,*/ Context context, GatewayType type) {
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

        Gateways gateways = AndroidJsonStore.getInstance().loadSettings().getSync();

        switch (type) {
            case Remote:
                gateways.setRemote(gateway);
                break;
            /*case Local:
                // unlike the desktop version only allow one local GW config
                gateways.setLocal(new Gateway[] {gateway});
                break;*/
        }

        AndroidJsonStore.getInstance().loadSettings().getSync().setRemote(gateway);
        Log.e(TAG, "Saved gateway: " + gateway);

        return false;
    }


}

package com.erikdeveloper.passvault;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.erikdeveloper.passvault.couchbase.AndroidCBLStore;
import com.passvault.crypto.AESEngine;
import com.passvault.util.Account;
import com.passvault.util.couchbase.AccountsChanged;
import com.passvault.util.couchbase.SyncGatewayClient;
import com.passvault.util.model.Gateway;

import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";
    private String[] MIME_TEXT = {"text/*"};

    public static final String ACCOUNTS_LIST = "ACCOUNTS_LIST";
    public static final int ADD_ACCOUNT_CODE = 1;
    public static final int UPDATE_ACCOUNT_CODE = 2;

    public static final String GATEWAYS = "gateways";

    private ExpandableListView accountListView;
    private ClipboardManager clipboard;

    private static ArrayList<Account> accounts = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.erikdeveloper.passvault.R.layout.activity_main);
        accountListView = (ExpandableListView) findViewById(com.erikdeveloper.passvault.R.id.account_list_view);
        Button addAccountButton = (Button) findViewById(com.erikdeveloper.passvault.R.id.button_add_account);
        addAccountButton.setText(com.erikdeveloper.passvault.R.string.main_activity_add_account_button);
        final Button clearClipboardButton = (Button) findViewById(com.erikdeveloper.passvault.R.id.button_clear_clipboard);
        clearClipboardButton.setText(com.erikdeveloper.passvault.R.string.main_activity_clear_clipboard);

        clipboard = (ClipboardManager) getSystemService(this.CLIPBOARD_SERVICE);


        // create CBL instance this will be done once specifying key,afterwards all calls will
        // use AndroidCBLStore.getInstance()
        //AndroidCBLStore store = AndroidCBLStore.getInstance("TestKey1TestKey2", this);
        //accounts = new ArrayList<>();
        //store.resetAccounts(20, accounts, this);
        //AndroidCBLStore.getInstance().resetAccounts(20, accounts, this);
        Log.e(TAG, "Accounts size = " + accounts.size());


        //Log.e(TAG, Integer.toString("TestKey1TestKey2".getBytes().length));
        //store.saveAccounts(accounts);
        //store.loadAccounts(accounts);
        //Collections.sort(accounts);

        Bundle b = this.getIntent().getExtras();
        if (b != null) {
            accounts = (ArrayList<Account>) b.getSerializable(ACCOUNTS_LIST);
            //MainActivity.getAccounts().remove(account);
        }

        // log any local conflicts
        AndroidCBLStore.getInstance().printConflicts();

        // check to see if purge deletes should be run
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.DB_PURGE_ON_DELETE_KEY), false)) {
            Log.e(TAG, "Purging database deletes");
            AndroidCBLStore.getInstance().purgeDeletes();
        }




        AccountExpandableListAdapter accountExpandableListAdapter = new AccountExpandableListAdapter(this, accounts);
        accountListView.setAdapter(accountExpandableListAdapter);

        accountListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                switch (childPosition) {
                    case AccountExpandableListAdapter.PASS:
                        saveToClipBoard(accounts.get(groupPosition).getPass());
                        accountListView.collapseGroup(groupPosition);
                        break;
                    case AccountExpandableListAdapter.OLD_PASS:
                        saveToClipBoard(accounts.get(groupPosition).getOldPass());
                        break;
                    case AccountExpandableListAdapter.EDIT:
                        //start Edit Account Intent
                        Intent updateAccountIntent = new Intent();
                        Bundle b = new Bundle();
                        b.putSerializable(EditAccountActivity.EDIT_ACCOUNT, accounts.get(groupPosition));
                        updateAccountIntent.putExtras(b);
                        updateAccountIntent.setClass(MainActivity.this, EditAccountActivity.class);
                        startActivityForResult(updateAccountIntent, UPDATE_ACCOUNT_CODE);
                        break;
                    case AccountExpandableListAdapter.DELETE:
                        deleteAccount(accounts.get(groupPosition), groupPosition);
                        break;
                }

                return false;
            }
        });


        addAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addAccountIntent = new Intent(MainActivity.this, AddAccountActivity.class);
                startActivityForResult(addAccountIntent, ADD_ACCOUNT_CODE);
            }
        });


        clearClipboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearClipBoard();
            }
        });
    }




    /*
     *  Options Menu Related Code
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.erikdeveloper.passvault.R.menu.main_menu, menu);
        menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Gateway free = SyncActivity.getGateway(prefs, MainActivity.this, SyncActivity.GatewayType.Remote);
                Gateway local = SyncActivity.getGateway(prefs, MainActivity.this, SyncActivity.GatewayType.Local);

                if (free == null || (free.getServer() == null || free.getServer().equalsIgnoreCase("")))
                    menuItem.getSubMenu().getItem(0).setEnabled(false);
                else
                    menuItem.getSubMenu().getItem(0).setEnabled(true);
                    //Log.e(TAG, ")))))))) free not enabled");

                if (local == null || (local.getServer() == null || local.getServer().equalsIgnoreCase("")))
                    menuItem.getSubMenu().getItem(1).setEnabled(false);
                else
                    menuItem.getSubMenu().getItem(1).setEnabled(true);
                    //Log.e(TAG, ")))))))) persoanl not enabled");

                return true;
            }
        });
        //menu.getItem(1).getSubMenu().getItem(1).setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case com.erikdeveloper.passvault.R.id.menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            /*case com.developernot.passvault.R.id.menu_sync:
                syncPasswords();
                return true;*/
            case com.erikdeveloper.passvault.R.id.menu_sync_free:
                syncPasswords(SyncActivity.GatewayType.Remote);
                return true;
            case com.erikdeveloper.passvault.R.id.menu_sync_personal:
                syncPasswords(SyncActivity.GatewayType.Local);
                return true;
            case com.erikdeveloper.passvault.R.id.menu_key:
                showChangeKeyDialog();
                return true;
            case com.erikdeveloper.passvault.R.id.menu_exit:
                clearClipBoard();
                ExitActivity.exitApplication(this);
                //finish();
                //android.os.Process.killProcess(android.os.Process.myPid());
                //super.onDestroy();
                //android.os.Process.killProcess(android.os.Process.myPid());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED)
            return;

        if (requestCode == ADD_ACCOUNT_CODE) {

            if (resultCode == RESULT_OK) {
                Collections.sort(accounts);
                ((AccountExpandableListAdapter) accountListView.getExpandableListAdapter()).notifyDataSetChanged();
            } else {
                // check for android.view.WindowManager$BadTokenException: Unable to add window — token android.os.BinderProxy@
                if (MainActivity.this.isFinishing() || MainActivity.this.isDestroyed())
                    Toast.makeText(MainActivity.this, "Failed to Add Account", Toast.LENGTH_LONG).show();
                else
                    showAlertDialogIntentFailed("Add Account Error", "Failed to Add Account");
            }

        } else if (requestCode == UPDATE_ACCOUNT_CODE) {

            if (resultCode == RESULT_OK) {
                Collections.sort(accounts);
                ((AccountExpandableListAdapter) accountListView.getExpandableListAdapter()).notifyDataSetChanged();
            } else {
                // check for android.view.WindowManager$BadTokenException: Unable to add window — token android.os.BinderProxy@
                if (MainActivity.this.isFinishing() || MainActivity.this.isDestroyed())
                    Toast.makeText(MainActivity.this, "Failed to Update Account", Toast.LENGTH_LONG).show();
                else
                    showAlertDialogIntentFailed("Update Account Error", "Failed to Update Account");
            }

        }
    }


    /*
     * Utility Methods
     */

    /*
     * Work around for not being able to clear the clipboard data through any ClipBoard API that
     * I could find. With my S5 appears to save a history of 20 items, other implementations maybe
     * different.
     */
    private void clearClipBoard() {
        int numberOfClipToDelete;

        // in case of non integer, default to 20
        try {
            numberOfClipToDelete = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.CLIP_NUMBER_REMOVE_KEY), "20"));
        } catch (NumberFormatException e) {
            numberOfClipToDelete = 20;
        }

        // in my test a large number crashed the system
        if (numberOfClipToDelete > 25)
            numberOfClipToDelete = 20;

        for (int i=1; i <= numberOfClipToDelete; i++) {
            StringBuilder clipContent = new StringBuilder("");

            for (int j=0; j < i; j++)
                clipContent.append(" ");

            saveToClipBoard(clipContent.toString());
        }
    }

    private void saveToClipBoard(String itemToSave) {
        ClipData.Item item = new ClipData.Item(itemToSave);
        ClipData data = new ClipData(TAG, MIME_TEXT, item);
        clipboard.setPrimaryClip(data);
    }


    private void deleteAccount(final Account account, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Confirm you wish to delete: " + account.getName())
                .setTitle(com.erikdeveloper.passvault.R.string.delete_account_title);

        builder.setPositiveButton(com.erikdeveloper.passvault.R.string.delete_account_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                accountListView.collapseGroup(position);
                //remove account from UI
                accounts.remove(account);
                ((AccountExpandableListAdapter)accountListView.getExpandableListAdapter()).notifyDataSetChanged();
                //remove account from CBL
                AndroidCBLStore.getInstance().deleteAccount(account);

                Toast.makeText(MainActivity.this, "Account " + account.getName() + " Deleted.",
                        Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton(com.erikdeveloper.passvault.R.string.delete_account_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public static ArrayList<Account> getAccounts() {
        return accounts;
    }


    private void syncPasswords(SyncActivity.GatewayType type) {

        /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        final String host = prefs.getString(getString(com.developernot.passvault.R.string.SYNC_URL_KEY), SyncGatewayClient.DEFAULT_HOST);
        final int port = Integer.parseInt(prefs.getString(getString(com.developernot.passvault.R.string.SYNC_PORT_KEY), String.valueOf(SyncGatewayClient.DEFAULT_PORT)));
        final String bucket = prefs.getString(getString(com.developernot.passvault.R.string.SYNC_BUCKET_KEY), SyncGatewayClient.DEFAULT_BUCKET);
        final String user = prefs.getString(getString(R.string.SYNC_USER_KEY), null);
        final String pass = prefs.getString(getString(R.string.SYNC_PASS_KEY), null);
        final String protocol = prefs.getString(getString(R.string.SYNC_PROTOCOL_KEY), SyncGatewayClient.DEFAULT_PROTOCOL);
        */
        //start test

        // test calling register server, maybe some issues with keystore
        /*
        new Thread() {
            @Override
            public void run() {
                super.run();
                String reg = "ec2-54-193-22-33.us-west-1.compute.amazonaws.com:8443";
                RegisterAccount regAccount = new RegisterAccount(reg);
                RegisterResponse resp = regAccount.registerV1("new3@mail.com", "password");
                Log.e(TAG, ">>>>>>>>>>>>>>> reg success = " + resp.success());

                if (resp.success()) {
                    Object returnV = resp.getReturnValue();

                    if (returnV instanceof Gateway) {
                        Log.e(TAG, " >>>>> Gateway config = " + ((Gateway)returnV).toString());
                    } else {
                        Log.e(TAG, " >>>>> Not Gateway Instance, " + returnV.getClass().getName());
                    }
                } else {
                    Log.e(TAG, " >>>>> Error: " + resp.getError());
                }
            }
        }.start();
        */



        // test Jackson json serialization in android - PASS
        /*
        Gateway local = new Gateway();
        local.setBucket(bucket);
        local.setPort(port);
        local.setProtocol(protocol);
        local.setServer(host);
        local.setUserName(user);
        local.setPassword(pass);
        Gateway remote = new Gateway();
        remote.setBucket("bucket_remote");
        remote.setPort(4984);
        remote.setProtocol("https");
        remote.setServer("remotehost.com");
        remote.setPassword("pass");
        remote.setUserName("user1@mail.com");
        Gateways gateways = new Gateways();
        gateways.setRemote(remote);
        gateways.setLocal(new Gateway[]{local});
        ObjectMapper mapper = new ObjectMapper();

        try {
            prefs.edit().putString("gateways", mapper.writeValueAsString(gateways)).commit();
        } catch (Exception e) {e.printStackTrace();}


        String gws = prefs.getString("gateways", null);
        ObjectMapper mapper2 = new ObjectMapper();
        Gateways gateways2 = null;

        try {
            gateways2 = mapper2.readValue(gws, Gateways.class);
        } catch (Exception e) {e.printStackTrace(); gateways2 = null;}

        if (gateways2 != null) {
            Log.e(TAG, gateways2.getRemote().toString());
            Log.e(TAG, gateways2.getLocal()[0].toString());
        }
        */
        //end test
/*
        final AccountsChanged accountsChgImpl = new AccountsChanged() {
            @Override
            public void onAccountsChanged() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        accounts.clear();

                        try {
                            AndroidCBLStore.getInstance().loadAccounts(accounts);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Collections.sort(accounts);
                        ((AccountExpandableListAdapter) accountListView.getExpandableListAdapter()).notifyDataSetChanged();
                    }
                });

            }
        };
*/

        // Test running as async task

        class SyncAccounts extends AsyncTask<Gateway, Void, SyncGatewayClient.ReplicationStatus> {
            SyncGatewayClient.ReplicationStatus status = null;
            AccountsChanged accountsChgImpl = null;


            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                /*ccountsChgImpl = new AccountsChanged() {
                    @Override
                    public void onAccountsChanged() {
                        /*
                        accounts.clear();

                        try {
                            AndroidCBLStore.getInstance().loadAccounts(accounts);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Collections.sort(accounts);
                        ((AccountExpandableListAdapter) accountListView.getExpandableListAdapter()).notifyDataSetChanged();
                        */
                        /*
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                accounts.clear();

                                try {
                                    AndroidCBLStore.getInstance().loadAccounts(accounts);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Collections.sort(accounts);
                                ((AccountExpandableListAdapter) accountListView.getExpandableListAdapter()).notifyDataSetChanged();
                            }
                        });
                        */
                    /*}
                };*/
            }

            @Override
            protected SyncGatewayClient.ReplicationStatus doInBackground(Gateway... gateways) {
                Gateway gateway = gateways[0];

                status = AndroidCBLStore.getInstance()
                        .syncAccounts(gateway.getServer(), gateway.getProtocol(), gateway.getPort(), gateway.getBucket(),
                                gateway.getUserName(), gateway.getPassword(), accountsChgImpl);

                do {
                    try {
                        Thread.currentThread().sleep(1_000L);
                        Log.e(TAG, ">>>>>> Status=" + status.isRunning());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        //showAlertDialogIntentFailed("Possible Replication Error", e.getMessage());
                    }
                } while (status.isRunning());

                return status;
            }

            @Override
            protected void onPostExecute(SyncGatewayClient.ReplicationStatus replicationStatus) {
                super.onPostExecute(replicationStatus);
                boolean success = true;
                StringBuilder error = new StringBuilder();

                if (replicationStatus.getPullError() != null) {
                    success = false;
                    error.append(replicationStatus.getPullError().getMessage() + "\n");
                }

                if (replicationStatus.getPushError() != null) {
                    success = false;
                    error.append(replicationStatus.getPushError().getMessage());
                }

                if (success) {
                    accounts.clear();

                    try {
                        AndroidCBLStore.getInstance().loadAccounts(accounts);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Collections.sort(accounts);
                    ((AccountExpandableListAdapter) accountListView.getExpandableListAdapter()).notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Password synchronization complete", Toast.LENGTH_LONG).show();

                } else {
                    // check for android.view.WindowManager$BadTokenException: Unable to add window — token android.os.BinderProxy@
                    if (MainActivity.this.isFinishing() || MainActivity.this.isDestroyed())
                        Toast.makeText(MainActivity.this, "Synchronization Error:\n" + error.toString(), Toast.LENGTH_LONG).show();
                    else
                        showAlertDialogIntentFailed("Synchronization Error", error.toString());

                }
            }
        }

        //new SyncAccounts().execute(SyncActivity.getGateway(prefs, MainActivity.this, SyncActivity.GatewayType.Remote));

        new SyncAccounts().execute(SyncActivity.getGateway(
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this),
                MainActivity.this,
                type));

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


    private void showChangeKeyDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(com.erikdeveloper.passvault.R.layout.dialog_change_key);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(com.erikdeveloper.passvault.R.string.dialog_change_key_title);
        final TextView statusView = (TextView) dialog.findViewById(com.erikdeveloper.passvault.R.id.dialog_change_key_status_textview);
        final EditText enterKeyEditText = (EditText) dialog.findViewById(com.erikdeveloper.passvault.R.id.dialog_change_key_enter_key_edittext);
        final EditText reenterKeyEditText = (EditText) dialog.findViewById(com.erikdeveloper.passvault.R.id.dialog_change_key_reenter_key_edittext);
        Button enterButton = (Button) dialog.findViewById(com.erikdeveloper.passvault.R.id.dialog_change_key_enter_button);
        Button cancelButton = (Button) dialog.findViewById(com.erikdeveloper.passvault.R.id.dialog_change_key_cancel_button);

        class MyFocusChangeListerner implements View.OnFocusChangeListener {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (enterKeyEditText.getText().toString().equals(reenterKeyEditText.getText().toString()))
                        statusView.setText(com.erikdeveloper.passvault.R.string.dialog_change_key_keys_match);
                    else
                        statusView.setText(com.erikdeveloper.passvault.R.string.dialog_change_key_keys_dont_match);
                }
            }
        }

        class MyTextWatcher implements TextWatcher {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (enterKeyEditText.getText().toString().equals(reenterKeyEditText.getText().toString()))
                    statusView.setText(com.erikdeveloper.passvault.R.string.dialog_change_key_keys_match);
                else
                    statusView.setText(com.erikdeveloper.passvault.R.string.dialog_change_key_keys_dont_match);
            }
        }

        MyFocusChangeListerner myFocusChangeListerner = new MyFocusChangeListerner();
        MyTextWatcher myTextWatcher = new MyTextWatcher();
        reenterKeyEditText.setOnFocusChangeListener(myFocusChangeListerner);
        reenterKeyEditText.addTextChangedListener(myTextWatcher);
        enterKeyEditText.setOnFocusChangeListener(myFocusChangeListerner);
        enterKeyEditText.addTextChangedListener(myTextWatcher);

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (enterKeyEditText.getText().toString().equals(reenterKeyEditText.getText().toString())) {
                    try {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        int length = Integer.parseInt(prefs.getString(getString(com.erikdeveloper.passvault.R.string.ENCRYPTION_KEY_LENGTH_KEY),
                                getString(com.erikdeveloper.passvault.R.string.DEFAULT_ENCRYPTION_KEY_LENGTH)));
                        String finalKey = AESEngine.finalizeKey(enterKeyEditText.getText().toString(), length);
                        Log.e(TAG, "finalKey=" + finalKey);
                        AndroidCBLStore.getInstance().setEncryptionKey(finalKey);
                        AndroidCBLStore.getInstance().saveAccounts(accounts);
                        dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlertDialogIntentFailed("Change Key Failed", e.getMessage());
                    }
                } else {
                    showAlertDialogIntentFailed("Change Key Failed", "Keys don't match");
                }

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}

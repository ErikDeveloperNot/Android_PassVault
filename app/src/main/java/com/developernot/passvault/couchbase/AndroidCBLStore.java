package com.developernot.passvault.couchbase;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.passvault.util.Account;
import com.passvault.util.AccountUUIDResolver;
import com.passvault.util.Utils;
import com.passvault.util.couchbase.AccountsChanged;
import com.passvault.util.couchbase.CBLStore;
import com.passvault.util.couchbase.SyncGatewayClient;

import java.util.List;

/**
 * Created by erik.manor on 5/7/17.
 *
 * Singleton Pattern - create instance will only be called once since it will be called
 *                     by main thread so know sync issues need to be worried about.
 */

public class AndroidCBLStore extends CBLStore implements AccountUUIDResolver {

    private static AndroidCBLStore androidCBLStore;
    private static String TAG = "AndroidCBLStore";
    private static final String ACCOUNT_UUID_KEY = "ACCOUNT_UUID_KEY";

    //private Database database;
    //private String encryptionKey;
    //private String databaseName = "pass_vault";
    //private String databaseFormat = "SQLite";
    private Context context;

    public static enum DatabaseFormat {SQLite, ForestDB}

    public static void destroyInstance() {
        //chz hack, but i control when it is called so no concurrency worries
        androidCBLStore = null;
    }

    public static AndroidCBLStore getInstance() {
        return androidCBLStore;
    }

    public Database getDatabase() {
        return database;
    }

    public static AndroidCBLStore getInstance(String encryptionKey, Context context) {
        //no need to worry about concurrency
        if (androidCBLStore == null)
            androidCBLStore = new AndroidCBLStore(encryptionKey, context);

        return androidCBLStore;
    }

    private AndroidCBLStore(String encryptionKey, Context context) {
        this.encryptionKey = encryptionKey;
        this.databaseName = "pass_vault";
        this.databaseFormat = "SQLite";
        this.context = context;

        DatabaseOptions dbOptions = new DatabaseOptions();
        dbOptions.setCreate(true);
        dbOptions.setStorageType(databaseFormat);
        dbOptions.setReadOnly(false);


        try {
            Manager manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
            database = manager.openDatabase(databaseName, dbOptions);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//prefs.edit().putString(ACCOUNT_UUID_KEY, "").commit();
        if (!prefs.contains(ACCOUNT_UUID_KEY))
            prefs.edit().putString(ACCOUNT_UUID_KEY, "").commit();

        Utils.setAccountUUIDResolver(this);
        accountUUID = prefs.getString(ACCOUNT_UUID_KEY, "");

    }


    public String getEncryptionKey() {
        return this.encryptionKey;
    }


    // Android does not have java.util.Base64 so use android.util.Base64 - no other changes
    @Override
    public byte[] decodeString(String toDecode) {

        if (toDecode == null || toDecode.equals("")) {
            return Base64.decode("", Base64.DEFAULT);
        } else {
            return Base64.decode(toDecode, Base64.DEFAULT);
        }
    }


    // Android does not have java.util.Base64 so use android.util.Base64 - no other changes
    @Override
    public byte[] encodeBytes(byte[] toEncode) {

        if (toEncode == null) {
            return Base64.encode(new byte[]{}, Base64.DEFAULT);
        } else {
            return Base64.encode(toEncode, Base64.DEFAULT);
        }

    }


    @Override
    public SyncGatewayClient.ReplicationStatus syncAccounts(String host, String protocol, int port, String bucket,
                                                            @Nullable String user, @Nullable String password, AccountsChanged accountsChanged) {

        if (user != null && !user.equalsIgnoreCase(""))
            return super.syncAccounts(host, protocol, port, bucket, user, password, accountsChanged);
        else
            return super.syncAccounts(host, protocol, port, bucket, accountsChanged);
    }


    @Override
    public void updateAccountUUID(List<Account> accounts, String uuid) {
        super.updateAccountUUID(accounts, uuid);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(ACCOUNT_UUID_KEY, uuid).commit();
    }


    @Override
    public String getAccountUUID() {
Log.e(TAG, "------------ Returing accountUUID: " + PreferenceManager.getDefaultSharedPreferences(context).getString(ACCOUNT_UUID_KEY, ""));
        return PreferenceManager.getDefaultSharedPreferences(context).getString(ACCOUNT_UUID_KEY, "");
    }

    //just for testing
    public void resetAccounts(int accountCount, List<Account> accounts, Context context) {
        try {
            Log.e(TAG, "Resetting Accounts/Database");
            database.delete();
            DatabaseOptions dbOptions = new DatabaseOptions();
            dbOptions.setCreate(true);
            dbOptions.setStorageType(databaseFormat);
            dbOptions.setReadOnly(false);

            Log.e(TAG, "Creating new Database");
            Manager manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
            database = manager.openDatabase(databaseName, dbOptions);

            for (int i=0; i<accountCount; i++) {
                Account account = new Account("Test_" + i, "User_" + i, "pass_" + i, "pass_" + i, "",
                        System.currentTimeMillis());
                accounts.add(account);
            }

            Log.e(TAG, "Database Populated...");

            for (Account account: accounts) {
                Log.e(TAG, "Acct Name: " + account.getName());
                Log.e(TAG, "Acct User: " + account.getUser());
                Log.e(TAG, "Acct Pass: " + account.getPass());
                Log.e(TAG, "Acct Old Pass: " + account.getOldPass());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

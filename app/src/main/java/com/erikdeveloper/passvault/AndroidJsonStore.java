package com.erikdeveloper.passvault;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.passvault.util.data.file.JsonStore;
import com.passvault.util.data.file.model.Store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by user1 on 11/22/17.
 *
 * Change persistence directory and Android does not have java.nio
 */

public class AndroidJsonStore extends JsonStore {


    private static AndroidJsonStore jsonStore;
    private static Context context;
    private static String TAG = "AndroidJsonStore";
    private static String PERSISTENCE_DIR = "data";
    private static String DATABASE_NAME = "data.json";


    public static AndroidJsonStore getInstance(String key) {
        // single threaded don't worry about concurrency when null
        if (jsonStore == null) {
            jsonStore = new AndroidJsonStore();
            jsonStore.setEncryptionKey(key);
        }

        return jsonStore;
    }

    public static AndroidJsonStore getInstance(Context context) {
        AndroidJsonStore.context = context;
        // single threaded don't worry about concurrency when null
        if (jsonStore == null) {
            jsonStore = new AndroidJsonStore();
        }

        File baseDirectory = context.getFilesDir();
        for (File file : baseDirectory.listFiles()) {
            System.out.println("File: " + file.getName() + ", directory: " + file.isDirectory());
        }

        return jsonStore;
    }

    public static AndroidJsonStore getInstance() {
        return jsonStore;
    }


    private AndroidJsonStore() {

    }

    @Override
    protected void rotateDataFiles() {
        // no reason to support on phone since it is a pain to get to trouble-shoot, plus size
    }

    @Override
    protected void writeDataFile(boolean updateVersion) {

        if (updateVersion)
            dataStore.setVersion(dataStore.getVersion() + 1);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT.INDENT_OUTPUT, true);

        try {
            objectMapper.writeValue(new File(context.getFilesDir(), dataFile), dataStore);
        } catch (IOException e) {
            Log.w(TAG, "Error saving datastore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected Store loadDataStore() {
        dataFile = PERSISTENCE_DIR + "/" + DATABASE_NAME;

        File baseDirectory = new File(context.getFilesDir(), PERSISTENCE_DIR);

        if (baseDirectory.length() <= 0) {
            baseDirectory.mkdir();
        }

        File file = new File(context.getFilesDir(), dataFile);
        long length = file.length();

        if (length > 0) {
            byte[] jsonData = new byte[(int)length];

            try {
                FileInputStream fis = new FileInputStream(file);
                fis.read(jsonData);
                ObjectMapper objectMapper = new ObjectMapper();
                dataStore = objectMapper.readValue(jsonData, Store.class);
            } catch (Exception e) {
                Log.w(TAG, "Error loading datastore: " + e.getMessage());
                e.printStackTrace();
                dataStore = new Store();
            }
        } else {
            dataStore = new Store();
        }

        return dataStore;
    }
}

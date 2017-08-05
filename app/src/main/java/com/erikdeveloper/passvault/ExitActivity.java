package com.erikdeveloper.passvault;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ExitActivity extends AppCompatActivity
{
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(android.os.Build.VERSION.SDK_INT >= 21)
        {
            finishAndRemoveTask();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        else
        {
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public static void exitApplication(Context context)
    {
        Intent intent = new Intent(context, ExitActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        context.startActivity(intent);
    }
}

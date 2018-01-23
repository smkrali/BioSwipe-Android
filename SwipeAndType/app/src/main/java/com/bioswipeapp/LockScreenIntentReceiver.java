package com.bioswipeapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by cse498 on 10/7/15.
 */
public class LockScreenIntentReceiver extends BroadcastReceiver {

    public static int lastIntent = 0;
    // Handle actions and display Lockscreen
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && lastIntent!=1) {
            Log.v("Debug1", "on receive srcreen off");
            lastIntent = 1;
            start_lockscreen(context);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            lastIntent = 2;
            Log.v("Debug1", "on receive srcreen on");
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            lastIntent = 3;
            Log.v("Debug1", "on receive boot completed on");
        }

    }

    // Display lock screen
    private void start_lockscreen(Context context) {
        if (PatternLockUtils.hasPattern(context)) {
            Intent mIntent = new Intent(context, MainActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //mIntent.addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
            context.startActivity(mIntent);
        }
    }

}


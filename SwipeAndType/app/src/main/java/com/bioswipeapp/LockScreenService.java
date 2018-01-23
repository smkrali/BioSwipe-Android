package com.bioswipeapp;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

/**
 * Created by cse498 on 10/7/15.
 */
public class LockScreenService extends Service {

    private BroadcastReceiver mReceiver;
    private static KeyguardManager mKM;
    private static KeyguardManager.KeyguardLock mKL;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        mKL = mKM.newKeyguardLock("IN");;
    }

    // Register for Lockscreen event intents
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("Debug1", "service started");
        if (intent!=null && intent.getBooleanExtra("disable", false)==true) {
            Log.v("Debug1", "disable from intent serv");
//            mKL.disableKeyguard();
        }
        else if (intent!=null &&  intent.getBooleanExtra("enable", false)==true) {
            Log.v("Debug1", "enable from intent serv");
            //mKL.reenableKeyguard();

            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            mReceiver = new LockScreenIntentReceiver();
            registerReceiver(mReceiver, filter);

       /* if(intent == null || intent.getAction() == null) {
            Log.v("Debug", "service give up");

            return START_NOT_STICKY;
        } */

            startForeground();
        }
        return START_STICKY;
    }

    // Run service in foreground so it is less likely to be killed by system
    private void startForeground() {
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setTicker(getResources().getString(R.string.app_name))
                .setContentText("Running")
                .setContentIntent(null)
                .setOngoing(true)
                .build();
        startForeground(9999,notification);
    }

    // Unregister receiver
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}

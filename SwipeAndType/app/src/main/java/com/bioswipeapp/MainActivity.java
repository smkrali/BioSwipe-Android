package com.bioswipeapp;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends ConfirmPatternActivity implements SensorEventListener,
        LockScreenUtils.OnLockStatusChangedListener {
    public WindowManager winManager;
    public RelativeLayout wrapperView;
public static float last_x, last_y, last_z;
    public static float last_x_g, last_y_g, last_z_g;

    public static String usernameString;
    public static String settingString;
    public boolean clearEverything = false;
    public int mProgressLatest = -1;
    private static final int ADMIN_INTENT = 15;
    public static HomeKeyLocker homeKeyLoader;
        private Button btnUnlock;
        private SeekBar mProgressBar;
        // Member variables
        private static customViewGroup coverStatusBar;

    public static ArrayList<String> typingTouchRecords = new ArrayList<String>();

    public static DropboxAPI<AndroidAuthSession> mDBApi;

        // Set appropriate flags to make the screen appear over the keyguard
        @Override
        public void onAttachedToWindow() {
            Log.v("Angie1", "on atached to window");
            /*this.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG); */
/*            this.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            );*/

            super.onAttachedToWindow();
        }


    @Override
    protected boolean isPatternCorrect(List<PatternView.Cell> pattern) {
        return PatternLockUtils.isPatternCorrect(pattern, this);
    }

    @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.v("Angie1", "oncreate called");
            super.onCreate(savedInstanceState);
            PatternLockUtils.STAGE_INPUT = 1;
            PreferenceUtils.setAllWordRecordCountsToZero(this);
            if (!clearEverything) {
                // unlock screen in case of app get killed by system
                if (getIntent() != null && getIntent().hasExtra("kill")
                        && getIntent().getExtras().getInt("kill") == 1) {
                    enableKeyguard();
                    Log.v("Angie1", "kill intent");
                    unlockHomeButton();
                } else {

                    try {
                        // disable keyguard
                        //disableKeyguard();
                        makeFullScreen();

                        // lock home button
                        lockHomeButton();
                        Log.v("Angie1", "oncreate sending intent to enable the previous lock");
                        // start service for observing intents
                        Intent i = new Intent(this, LockScreenService.class);
                        i.putExtra("enable", true);
                        startService(i);

                        // listen the events get fired during the call
                        StateListener phoneStateListener = new StateListener();
                        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                        telephonyManager.listen(phoneStateListener,
                                PhoneStateListener.LISTEN_CALL_STATE);

                    } catch (Exception e) {
                    }

                }
            }
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString("accessToken", "jSJy2c-s8ewAAAAAAAA6aRmG9iiwGnOeacneLSyVjotqavKorv43vUaBX0I8cF8O");
        editor.apply();


        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String restoredText = prefs.getString("accessToken", "");
        if (restoredText!= "") {
            Log.v("Angie1", "retrieved access" + restoredText);
            AppKeyPair appKeys = new AppKeyPair(PreferenceUtils.APP_KEY, PreferenceUtils.APP_SECRET);
            AndroidAuthSession session = new AndroidAuthSession(appKeys, restoredText);
            mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        } else {

            AppKeyPair appKeys = new AppKeyPair(PreferenceUtils.APP_KEY, PreferenceUtils.APP_SECRET);
            AndroidAuthSession session = new AndroidAuthSession(appKeys);
            mDBApi = new DropboxAPI<AndroidAuthSession>(session);
            mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
        }

        SensorManager senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor senGyro = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        senSensorManager.registerListener(this, senGyro, SensorManager.SENSOR_DELAY_NORMAL);

    }

    protected void uploadRemainingFiles() {
        /* FILE WRITING CHANGES */
        //String extStorage = "storage/extSdCard/Android";
        String extStorage = Environment.getExternalStorageDirectory().toString();
        File dir = new File(extStorage + "/swipetype/" );
        String[] allFiles = dir.list();
        if (allFiles!=null) {
            for (int i = 0; i < allFiles.length; i++) {
                String currentFilename = allFiles[i];
                Log.v("Angie", "trying to upload saved file " + currentFilename);
                ConfirmPatternActivity.uploadSavedFile(this, currentFilename, Environment.getExternalStorageDirectory() + "/swipetype/" + currentFilename);
            }
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            last_x = x;
            last_y = y;
            last_z = z;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            //Log.v("Angie1", "got a gyroscope event");
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            last_x_g = x;
            last_y_g = y;
            last_z_g = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

        // Handle events of calls and unlock screen if necessary
        private class StateListener extends PhoneStateListener {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {

                super.onCallStateChanged(state, incomingNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        unlockHomeButton();
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        break;
                }
            }
        };

        // Don't finish Activity on Back press
        @Override
        public void onBackPressed() {
            return;
        }

    public void showSettings(View view) {
        Log.v("Angie1", "show settings clicked");
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Settings");
        final EditText username = new EditText(this);
        final EditText setting = new EditText(this);

        username.setHint("Username");
        setting.setHint("Setting");
        username.setInputType(InputType.TYPE_CLASS_TEXT);
        LinearLayout ll=new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(username);
        ll.addView(setting);
        alertDialog.setView(ll);

        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                Log.v("Angie1", "onclick clicked");
                usernameString = username.getText().toString();
                settingString = setting.getText().toString();
                settingsButton.setText("Settings (U:" + username.getText() + "S:" + setting.getText()+")");
                //ACTION
                //homeKeyLoader.lock(MainActivity.this);
                PatternUtils.generateSavedFileName(MainActivity.this);
            }
        });

        final AlertDialog alert = alertDialog.create();
        homeKeyLoader.unlock();
        alert.show();
    }
    public void makeFullScreen() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
       /* if(Build.VERSION.SDK_INT < 19) { //View.SYSTEM_UI_FLAG_IMMERSIVE is only on API 19+
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        } */
        //preventStatusBarExpansion(this);
        //preventBottomStatusBarExpansion(this);
    }

    public static void preventStatusBarExpansion(Context context) {
        if (coverStatusBar == null || !coverStatusBar.isShown()) {
           /* WindowManager manager = ((WindowManager) context.getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE));

            WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
            localLayoutParams.gravity = Gravity.TOP;
            /*localLayoutParams.flags = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR |

                    // this is to enable the notification to recieve touch events
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

                    // Draws over status bar
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

            localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            localLayoutParams.height = (int) (50 * context.getResources()
                    .getDisplayMetrics().scaledDensity);
            localLayoutParams.format = PixelFormat.TRANSPARENT;
*/
            //coverStatusBar = new customViewGroup(context);
//            manager.addView(coverStatusBar, localLayoutParams);

            //ImageView i = new ImageView(context);
            //i.setBackgroundColor(Color.RED);
            //manager.addView(i, localLayoutParams);

            Log.v("Angie1", "cover status bar added with x " + coverStatusBar.getX() +
                    " y " + coverStatusBar.getY() + " width" + coverStatusBar.getWidth()
                    + "height " + coverStatusBar.getHeight());
        }
    }


    public static class customViewGroup extends ViewGroup {

        public customViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            Log.v("Angie1", "**********Intercepted");
            return true;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.v("Angie1", "onwindow focused changed");
        if (hasFocus) {
           // lockHomeButton();
        }
        if (hasFocus && !clearEverything) {
        if (coverStatusBar == null || !coverStatusBar.isShown()) {
            Log.v("Angie1", "has focus");
            /*getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            makeFullScreen(); */
        }
        }  else {
        Log.v("Angie1", "has no focus");
       // WindowManager manager = ((WindowManager) getApplicationContext()
         //       .getSystemService(Context.WINDOW_SERVICE));
        if (coverStatusBar!=null && coverStatusBar.isShown()) {
            Log.v("Angie1", "coverStatusBar removed since it was shown");
            //manager.removeView(coverStatusBar);
            coverStatusBar.invalidate();
        } else {
            Log.v("Angie1", "in focus coverStatusBar is not shown");
        }
    }
}

    @Override
    public void onResume() {
        super.onResume();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        currentTimeText.setText("Current time is " + dateFormat.format(Calendar.getInstance().getTime()).toString());
        Log.v("Angie1", "onresume");
        //if (PatternLockUtils.hasPattern(this)) {
        rightButton.setText("Not now");
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        leftButton.setVisibility(View.GONE);


/*
            Log.v("Angie1", "No pattern yet");
            rightButton.setText("Set Pattern");
            rightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("Angie1", "click received in set pattern");
                    Log.v("Angie1", "click received in set pattern pattern utils");
                }
            }); */

        if (mDBApi.getSession().authenticationSuccessful()) {
            Log.v("Angie1", "authenticaion successful");
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();

                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.putString("accessToken", "jSJy2c-s8ewAAAAAAAA6aRmG9iiwGnOeacneLSyVjotqavKorv43vUaBX0I8cF8O");
                editor.apply();
                Log.v("Angie1", "saved access token as " + accessToken);

            } catch (IllegalStateException e) {
                Log.i("Angie1", "Error authenticating", e);
            }
        }

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            Log.v("Angie", "wifi to uplaod remaining files ");
            uploadRemainingFiles();
        } else {
            Log.v("Angie", "no wifi to uplaod remaining files ");
        }
    }


    // Handle button clicks
        @Override
        public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

            if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                    || (keyCode == KeyEvent.KEYCODE_POWER)
                    || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                    || (keyCode == KeyEvent.KEYCODE_CAMERA)) {
                return true;
            }
            if ((keyCode == KeyEvent.KEYCODE_HOME)) {

                return true;
            }

            return false;

        }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        String giantstring = "";
        giantstring+= event.getEventTime() + ",";
        giantstring+= event.getX() + ",";
        giantstring+= event.getY() + ",";
        giantstring+= event.getPressure() + ",";
        giantstring+= MainActivity.last_x+",";
        giantstring+= MainActivity.last_y+",";
        giantstring+= MainActivity.last_z+",";
        giantstring+= MainActivity.last_x_g+",";
        giantstring+= MainActivity.last_y_g+",";
        giantstring+= MainActivity.last_z_g+",";
        giantstring+= event.getSize()+",";
        giantstring+= event.getTouchMajor()+",";
        giantstring+= event.getTouchMinor()+",";
        giantstring+= event.getToolMajor()+",";
        giantstring+= event.getToolMinor();//Kamran, removing extra return character "\n"
        typingTouchRecords.add(giantstring);

            return false;
    }
        // handle the key press events here itself
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                    || (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
                    || (event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
                return false;
            }
            if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {

                return true;
            }
            Log.v("Debug", "key was pressed" );
            return false;
        }





    // Simply unlock device when home button is successfully unlocked
    @Override
    public void onLockStatusChanged(boolean isLocked) {
        if (!isLocked) {

            //unlockDevice();
        }
    }

    @Override
    protected void onStop() {
        Log.v("Debug1", "onstop");
        super.onStop();
        //unlockHomeButton();
    }

    // Lock home button
    public void lockHomeButton() {
        if (homeKeyLoader == null) {
            homeKeyLoader = new HomeKeyLocker();
        }
        Log.v("Debug1", "locking hard home button");
        //homeKeyLoader.lock(this);

    }

    // Unlock home button and wait for its callback
    public void unlockHomeButton() {
        //homeKeyLoader.unlock();
        Log.v("Debug1", "unlocking hard home button");
/*        if (mLockscreenUtils != null) {
            mLockscreenUtils.unlock();
        } */
    }



    @SuppressWarnings("deprecation")
    private void disableKeyguard() {
        KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
        mKL.disableKeyguard();
    }

    @SuppressWarnings("deprecation")
    private void enableKeyguard() {
        KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
        mKL.reenableKeyguard();
    }

    //Simply unlock device by finishing the activity
    private void unlockDevice()
    {
/*        Log.v("Debug1", "trying to unlock progress " + mProgressBar.getProgress());
        if (mProgressBar.getProgress() > 50 && mProgressLatest != mProgressBar.getProgress()) {
 */           Log.v("Debug1", "progress good, calling ondestroy");
   //         mProgressLatest = mProgressBar.getProgress();
            onDestroy();
     //   }
    }


    public void onDestroy()
    {
        homeKeyLoader.unlock();

        Intent i = new Intent(this, LockScreenService.class);
        i.putExtra("disable", true);
        startService(i);
        WindowManager manager = ((WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));
        if (coverStatusBar!= null && coverStatusBar.isShown()) {
            Log.v("Debug1", "coverStatusBar removed since it was shown");
            //manager.removeView(coverStatusBar);
        } else {
            Log.v("Debug1", "in ondestroy coverStatusBar is not shown");
        }
//            android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
        finish();
    }

}
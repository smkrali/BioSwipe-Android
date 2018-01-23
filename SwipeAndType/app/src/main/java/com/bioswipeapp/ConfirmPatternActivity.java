/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.bioswipeapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.dropbox.client2.DropboxAPI;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

// For AOSP implementations, see:
// https://android.googlesource.com/platform/packages/apps/Settings/+/master/src/com/android/settings/ConfirmLockPattern.java
// https://android.googlesource.com/platform/frameworks/base/+/43d8451/policy/src/com/android/internal/policy/impl/keyguard/KeyguardPatternView.java
// https://android.googlesource.com/platform/frameworks/base/+/master/packages/Keyguard/src/com/android/keyguard/KeyguardPatternView.java
public class ConfirmPatternActivity extends BasePatternActivity
        implements PatternView.OnPatternListener {

    private static final String KEY_NUM_FAILED_ATTEMPTS = "num_failed_attempts";

    public static final int RESULT_FORGOT_PASSWORD = RESULT_FIRST_USER;

    protected int numFailedAttempts;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static ConfirmPatternActivity currentActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);

        messageText.setText("Swipe 1 out of 3");
        patternView.setInStealthMode(isStealthModeEnabled());
        patternView.setOnPatternListener(this);
        leftButton.setText(R.string.pl_cancel);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        currentActivity = this;

        rightButton.setText(R.string.pl_forgot_pattern);

        if (savedInstanceState == null) {
            numFailedAttempts = 0;
        } else {
            numFailedAttempts = savedInstanceState.getInt(KEY_NUM_FAILED_ATTEMPTS);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_NUM_FAILED_ATTEMPTS, numFailedAttempts);
    }

    @Override
    public void onPatternStart() {
        PatternView.allStrings.clear();
        Log.v("Debug1", "on pattern start");
        removeClearPatternRunnable();

        // Set display mode to correct to ensure that pattern can be in stealth mode.
        patternView.setDisplayMode(PatternView.DisplayMode.Correct);
    }

    @Override
    public void onPatternCellAdded(List<PatternView.Cell> pattern) {
    }

    @Override
    public void onPatternDetected(List<PatternView.Cell> pattern) {
        if (isPatternCorrect(pattern)) {
            currentActivity = this;
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
                Log.v("Debug3", "we have wifi");
                uploadFile(ConfirmPatternActivity.this);
            } else {
                //save file
                Log.v("Debug3", "we have no wifi");
                saveFile(ConfirmPatternActivity.this);
            }
            onConfirmed();
        } else {
            messageText.setText(R.string.pl_wrong_pattern);
            patternView.setDisplayMode(PatternView.DisplayMode.Wrong);
            Log.v("Debug1", "sending a post clearn runnable from onpattern deteced" + PatternLockUtils.STAGE_INPUT);
            postClearPatternRunnable();
            PatternView.allStrings.clear();
            //ViewAccessibilityCompat.announceForAccessibility(messageText, messageText.getText());
            onWrongPattern();
        }
    }

    public static void advanceToNextStage() {
        PatternLockUtils.STAGE_INPUT++;
        Log.v("Debug", "advancing to next stage " + PatternLockUtils.STAGE_INPUT);
        currentActivity.postClearPatternRunnable();
        if (PatternLockUtils.STAGE_INPUT == 4) {
            Log.v("Debug5", "transitioning to keyboard entry stuff now");
        }
    }

    public static int verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            return 0;
        }
        return 1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

    Log.v("Debug1", "permission is granted");
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            Log.v("Debug1", "permission is granted1");
            if (grantResults != null && grantResults.length> 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v("Debug1", "permission is granted2");
                saveFile(ConfirmPatternActivity.this);
            } else {
                Log.v("Debug1", "permission is " + grantResults);
                if (grantResults != null && grantResults.length> 0)
                    Log.v("Debug1", "perission is " + grantResults[1]);
            }
        }
    }
                public static void saveFile(final Context c) {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Log.v("Debug1", "trying to save file in confirm patten with strings");
                    String everything = "";
                    if (PatternLockUtils.STAGE_INPUT >= 4) {
                        for (int i = 0; i < MainActivity.typingTouchRecords.size(); i++) {
                            everything += MainActivity.typingTouchRecords.get(i) + "\n";
                        }
                    } else {
                        for (int i = 0; i < PatternView.allStrings.size(); i++) {
                            everything += PatternView.allStrings.get(i) + "\n";
                        }
                    }
                    //out.close();

                    try {
                        Log.v("Debug6", "heres the string to save " + everything.substring(0,10));
                        /* FILE WRITING CHANGES */
                        //String extStorage = "storage/extSdCard/Android/";
                        String extStorage = Environment.getExternalStorageDirectory() + "/";
                        File dir = new File(extStorage+ "swipetype/" );
                        dir.mkdirs();
                        File file = new File(extStorage + "swipetype/" +
                                PatternUtils.convertSlashesToDash(PatternUtils.generateSavedFoldername(c)) +
                                PatternUtils.convertSlashesToDash(PatternUtils.generateSavedFileName(c)));

                        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false /*overwrite*/));
                        writer.write(everything.toString());
                        writer.close();

                        Log.i("Debug1", "file.getname " + file.getAbsolutePath());

                        if (PatternLockUtils.STAGE_INPUT >= 4) {
                            PreferenceUtils.incrimentWordFileCount(c);
                        }
                        if (PatternLockUtils.STAGE_INPUT >= 4) {
                            MainActivity.typingTouchRecords.clear();
                        } else {
                            PatternView.allStrings.clear();
                        }
                        advanceToNextStage();
                        //file.delete();
                    } catch (Exception e) {
                        Log.v("Debug2", "had a problem writing file " + e.getLocalizedMessage());
                        MainActivity.typingTouchRecords.clear();
                        PatternView.allStrings.clear();
                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }


            }
        });
        thread.start();
    }

    public static void uploadSavedFile(final Context c, final String shortenedFileName, final String filename) {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                File  f= new File(filename);
                    try {
                        InputStream stream = new FileInputStream(filename);

                        DropboxAPI.Entry response = MainActivity.mDBApi.putFile(PatternUtils.convertDashToSlaashes(shortenedFileName),
                                stream,
                                f.length(), null, null);
                        Log.i("Debug1", "The uploaded file's rev is: " + response.rev);
                        f.delete();

                    } catch(Exception e) {
                        Log.i("Debug1", "Upload failed " + e.getLocalizedMessage() + " e" + e.getMessage() + " " + e.toString());

                    }
            }
        });
        thread.start();
    }
    public static void uploadFile(final Context c) {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Log.v("Debug1", "trying to uplad file in confirm patten with strings");
                    String everything = "";
                    //"storage/extSdCard/Android/"
                    //Environment.getExternalStorageDirectory() + "/"
                    //File file = new File(Environment.getExternalStorageDirectory() + "/" +
                    //        PatternUtils.generateSavedFileName(ConfirmPatternActivity.this));
                    if (PatternLockUtils.STAGE_INPUT >= 4) {
                        for (int i = 0; i < MainActivity.typingTouchRecords.size(); i++) {
                            everything += MainActivity.typingTouchRecords.get(i) + "\n";
                        }
                    } else {
                        for (int i = 0; i < PatternView.allStrings.size(); i++) {
                            everything += PatternView.allStrings.get(i) + "\n";
                        }
                    }
                        //out.close();

                    try {
                        InputStream stream = new ByteArrayInputStream(everything.getBytes(StandardCharsets.UTF_8));
                        Log.v("Debug6", "heres the string to dropbox " + everything.substring(0,10));

                        //FileInputStream inputStream = new FileInputStream(file);
                        //Log.i("Debug1", "file.getname " + file.getName());
                        DropboxAPI.Entry response = MainActivity.mDBApi.putFile(
                                PatternUtils.generateSavedFoldername(c)+
                                        PatternUtils.generateSavedFileName(c), stream,
                                everything.length(), null, null);

//                        DropboxAPI.Entry response = MainActivity.mDBApi.putFile(file.getName(), inputStream,
//                                file.length(), null, null);
                        Log.i("Debug1", "The uploaded file's rev is: " + response.rev);
                        if (PatternLockUtils.STAGE_INPUT >= 4) {
                            PreferenceUtils.incrimentWordFileCount(c);
                        }
                        if (PatternLockUtils.STAGE_INPUT >= 4) {
                            MainActivity.typingTouchRecords.clear();
                        } else {
                            PatternView.allStrings.clear();
                        }
                        advanceToNextStage();
                        //file.delete();
                    } catch(Exception e) {
                        Log.i("Debug1", "Upload failed " + e.getLocalizedMessage() + " e" + e.getMessage() + " " + e.toString());
                        MainActivity.typingTouchRecords.clear();
                        PatternView.allStrings.clear();
                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }


            }
        });
        thread.start();
    }
    @Override
    public void onPatternCleared() {
        removeClearPatternRunnable();
    }

    protected boolean isStealthModeEnabled() {
        return false;
    }

    protected boolean isPatternCorrect(List<PatternView.Cell> pattern) {
        return true;
    }

    protected void onConfirmed() {
        setResult(RESULT_OK);
        //postClearPatternRunnable();
        //finish();
    }

    protected void onWrongPattern() {
        ++numFailedAttempts;
    }

    protected void onCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    protected void onForgotPassword() {
        setResult(RESULT_FORGOT_PASSWORD);
        finish();
    }

}

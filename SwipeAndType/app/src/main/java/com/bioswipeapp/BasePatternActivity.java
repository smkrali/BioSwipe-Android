/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.bioswipeapp;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class BasePatternActivity extends Activity  implements AdapterView.OnItemSelectedListener {

    private static final int CLEAR_PATTERN_DELAY_MILLI = 2000;
    private static final int NEXT_STAGE_DELAY_MILLI = 200;
    protected Button settingsButton;
    protected TextView messageText;
    protected PatternView patternView;
    protected TextView currentTimeText;
    protected LinearLayout buttonContainer;
    protected Button leftButton;
    protected Button rightButton;
    protected ImageView softKeyboard;
    protected Spinner spinner;
    public static String currentWord="";
    String[] positions;
    public static String currentActivity="unknown";

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Log.v("Debug3", "we selected " + positions[pos]);
        currentActivity = positions[pos];
        if (pos!=0) {
            patternView.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.INVISIBLE);
        } else {
            patternView.setVisibility(View.INVISIBLE);
            spinner.setVisibility(View.VISIBLE);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        Log.v("Debug3", "nothing selected ");
    }

    private final Runnable clearPatternRunnable = new Runnable() {
        public void run() {
            // clearPattern() resets display mode to DisplayMode.Correct.
            patternView.clearPattern();
        }
    };

    private final Runnable p1PatternRunnable = new Runnable() {
        public void run() {
            // clearPattern() resets display mode to DisplayMode.Correct.
            patternView.setPatternToP1();
            messageText.setText("Swipe " + (PatternLockUtils.STAGE_INPUT) + " out of 3");
        }
    };

    private final Runnable p2PatternRunnable = new Runnable() {
        public void run() {
            // clearPattern() resets display mode to DisplayMode.Correct.
            patternView.setPatternToP2();
            messageText.setText("Swipe " + (PatternLockUtils.STAGE_INPUT)+ " out of 3");
        }
    };

    private final Runnable p3PatternRunnable = new Runnable() {
        public void run() {
            // clearPattern() resets display mode to DisplayMode.Correct.
            patternView.setPatternToP3();
            messageText.setText("Swipe " + (PatternLockUtils.STAGE_INPUT) + " out of 3");
        }
    };

    private final Runnable textInputRunnable1 = new Runnable() {
        public void run() {
            // clearPattern() resets display mode to DisplayMode.Correct.
            patternView.setVisibility(View.GONE);
            softKeyboard.setVisibility(View.VISIBLE);
            leftButton.setVisibility(View.VISIBLE);
            rightButton.setVisibility(View.VISIBLE);
            leftButton.setText("Done");
            leftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    if (mWifi.isConnected()) {
                        Log.v("Debug3", "we have wifi");
                        ConfirmPatternActivity.uploadFile(BasePatternActivity.this);
                    } else {
                        //save file
                        Log.v("Debug3", "we have no wifi");
                        ConfirmPatternActivity.saveFile(BasePatternActivity.this);
                    }
                    Log.v("Debug1", "sending a post clearn runnable from left t1" + PatternLockUtils.STAGE_INPUT);
                    //postClearPatternRunnable();
                }
            });
            currentWord = PreferenceUtils.getTypedWordAtIndex(0);
            messageText.setText("Swipe the word: " + currentWord);
        }
    };

    private final Runnable textInputRunnable2 = new Runnable() {
        public void run() {
            // clearPattern() resets display mode to DisplayMode.Correct.
            softKeyboard.setVisibility(View.VISIBLE);
            leftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    if (mWifi.isConnected()) {
                        Log.v("Debug3", "we have wifi");
                        ConfirmPatternActivity.uploadFile(BasePatternActivity.this);
                    } else {
                        //save file
                        Log.v("Debug3", "we have no wifi");
                        ConfirmPatternActivity.saveFile(BasePatternActivity.this);
                    }
                    Log.v("Debug1", "sending a post clearn runnable from left t2" + PatternLockUtils.STAGE_INPUT);
                    //postClearPatternRunnable();
                }
            });
            currentWord = PreferenceUtils.getTypedWordAtIndex(1);
            messageText.setText("Swipe the word: " + currentWord);
        }
    };

    private final Runnable textInputRunnable3 = new Runnable() {
        public void run() {
            // clearPattern() resets display mode to DisplayMode.Correct.
            softKeyboard.setVisibility(View.VISIBLE);
            leftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    if (mWifi.isConnected()) {
                        Log.v("Debug3", "we have wifi");
                        ConfirmPatternActivity.uploadFile(BasePatternActivity.this);
                    } else {
                        //save file
                        Log.v("Debug3", "we have no wifi");
                        ConfirmPatternActivity.saveFile(BasePatternActivity.this);
                    }
                    finish();
                }
            });
            currentWord = PreferenceUtils.getTypedWordAtIndex(2);
            messageText.setText("Swipe the word: " + currentWord);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pl_base_pattern_activity);
        currentTimeText = (TextView) findViewById(R.id.current_time);
        messageText = (TextView)findViewById(R.id.pl_message_text);
        patternView = (PatternView)findViewById(R.id.pl_pattern);
        patternView.setVisibility(View.INVISIBLE);
        buttonContainer = (LinearLayout)findViewById(R.id.pl_button_container);
        leftButton = (Button)findViewById(R.id.pl_left_button);
        rightButton = (Button)findViewById(R.id.pl_right_button);
        softKeyboard = (ImageView) findViewById(R.id.soft_keyboard_view);
        //settingsButton = (Button) findViewById(R.id.settings);

        spinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        positions= getResources().getStringArray(R.array.planets_array);
    }

    protected void removeClearPatternRunnable() {
        patternView.removeCallbacks(clearPatternRunnable);
        patternView.removeCallbacks(p1PatternRunnable);
        patternView.removeCallbacks(p2PatternRunnable);
        patternView.removeCallbacks(p3PatternRunnable);
        patternView.removeCallbacks(textInputRunnable1);
        patternView.removeCallbacks(textInputRunnable2);
        patternView.removeCallbacks(textInputRunnable3);
    }

    protected void postClearPatternRunnable() {
        Log.v("Debug1", "post clearn runnable got stage " + PatternLockUtils.STAGE_INPUT);
        removeClearPatternRunnable();
        Log.v("Debug1", "post clearn runnable got stage 2 " + PatternLockUtils.STAGE_INPUT);
        if (PatternLockUtils.STAGE_INPUT == 1) {
            patternView.postDelayed(p1PatternRunnable, NEXT_STAGE_DELAY_MILLI);
        } else if (PatternLockUtils.STAGE_INPUT == 2) {
            patternView.postDelayed(p2PatternRunnable, NEXT_STAGE_DELAY_MILLI);
        } else if (PatternLockUtils.STAGE_INPUT == 3) {
            patternView.postDelayed(p3PatternRunnable, NEXT_STAGE_DELAY_MILLI);
        } else if (PatternLockUtils.STAGE_INPUT == 4) {
            patternView.postDelayed(textInputRunnable1, NEXT_STAGE_DELAY_MILLI);
            PreferenceUtils.findNextThreeWords(BasePatternActivity.this);
        } else if (PatternLockUtils.STAGE_INPUT == 5) {
            patternView.postDelayed(textInputRunnable2, NEXT_STAGE_DELAY_MILLI);
        } else if (PatternLockUtils.STAGE_INPUT == 6) {
            patternView.postDelayed(textInputRunnable3, NEXT_STAGE_DELAY_MILLI);
        } else if (PatternLockUtils.STAGE_INPUT == 7) {
            patternView.postDelayed(textInputRunnable3, NEXT_STAGE_DELAY_MILLI);
        } else if (PatternLockUtils.STAGE_INPUT == 8) {
        } else if (PatternLockUtils.STAGE_INPUT == 9) {
            patternView.postDelayed(clearPatternRunnable, CLEAR_PATTERN_DELAY_MILLI);
        }
    }
}

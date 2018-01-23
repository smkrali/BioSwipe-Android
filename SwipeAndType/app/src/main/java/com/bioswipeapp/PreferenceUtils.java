package com.bioswipeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by cse498 on 10/22/15.
 */
public class PreferenceUtils {
    public static final String APP_KEY = "\t\n" + "xn5dvo8qzongzt0";
    public static final String APP_SECRET = "\t\n" + "h29hhgox9mlxyyr";
    public static final ArrayList<String> allWords = new ArrayList<String>(
            Arrays.asList("where", "day", "because", "night", "you", "have", "good", "what",
                    "will", "thank", "bring", "work", "come"));

    /* "make", "there", "some",  "want", "night"
   /* public static final ArrayList<String> allWords = new ArrayList<String>(
            Arrays.asList("where", "day", "make", "there")); */


    public static List<String> threeKeywords = new ArrayList<String>();

    private PreferenceUtils() {}

    public static void setAllWordRecordCountsToZero(Context context) {
        if (!getBoolean("initialized", false, context)) {
            Log.v("Debug", "we were not already initialized so init to 0");
            putBoolean("initialized", true, context);
            for (int i = 1; i < allWords.size()+1; i++) {
                putInt("word" + i, 0, context);
            }
        }
    }

    public static void incrimentWordFileCount(Context context) {
        int index=0;
        for (int i=0; i<allWords.size(); i++) {
            if (allWords.get(i) == BasePatternActivity.currentWord) {
                index=i;
            }
        }
        int previousCount = getInt("word" + (index+1), 0, context);
        Log.v("Debug", "increased the count for word " + BasePatternActivity.currentWord +" index " + (index+1)
            + " to count " + previousCount+1);
        putInt("word" + (index + 1), previousCount + 1, context);
    }

    public static List<String> findNextThreeWords(Context context) {
        ArrayList<String> all = new ArrayList<String>();
        for (int i=1; i<allWords.size()+1; i++) {
            int CountOfWord = getInt("word"+i, 0, context);
            //Log.v("Debug", "for word " + i +" which is " + allWords.get(i-1) + "got count " + CountOfWord);
            //if (CountOfWord<2) {
                all.add(allWords.get(i-1));
            //}
        }
        long seed = System.nanoTime();
        Collections.shuffle(all, new Random(seed));
        if (all.size() <3) {
            for (int i=all.size(); all.size()<3; ) {
                all.add("done");
            }
        }
        threeKeywords = all.subList(0, 3);
        for (int i=0; i<threeKeywords.size(); i++) {
            Log.v("Debug", "the 3 words we got were " + i + threeKeywords.get(i));
        }
        return all.subList(0, 3);
    }

    public static String getTypedWordAtIndex(int index) {
        return threeKeywords.get(index);
    }
    public static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getString(String key, String defaultValue, Context context) {
        return getPreferences(context).getString(key, defaultValue);
    }

    public static Set<String> getStringSet(String key, Set<String> defaultValue, Context context) {
        return getPreferences(context).getStringSet(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue, Context context) {
        return getPreferences(context).getInt(key, defaultValue);
    }

    public static long getLong(String key, long defaultValue, Context context) {
        return getPreferences(context).getLong(key, defaultValue);
    }

    public static float getFloat(String key, float defaultValue, Context context) {
        return getPreferences(context).getFloat(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue, Context context) {
        return getPreferences(context).getBoolean(key, defaultValue);
    }

    public static SharedPreferences.Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

    public static void putString(String key, String value, Context context) {
        getEditor(context).putString(key, value).apply();
    }

    public static void putStringSet(String key, Set<String> value, Context context) {
        getEditor(context).putStringSet(key, value).apply();
    }

    public static void putInt(String key, int value, Context context) {
        getEditor(context).putInt(key, value).apply();
    }

    public static void putLong(String key, long value, Context context) {
        getEditor(context).putLong(key, value).apply();
    }

    public static void putFloat(String key, float value, Context context) {
        getEditor(context).putFloat(key, value).apply();
    }

    public static void putBoolean(String key, boolean value, Context context) {
        getEditor(context).putBoolean(key, value).apply();
    }

    public static void remove(String key, Context context) {
        getEditor(context).remove(key).apply();
    }

    public static void clear(Context context) {
        getEditor(context).clear().apply();
    }
}

package com.tangledbytes.phoneblocker.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private final SharedPreferences prefs;

    public AppPreferences(Context cxt) {
        prefs = cxt.getSharedPreferences(Constants.APP_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void changePreference(String key, String newValue) {
        prefs.edit().putString(key, newValue).apply();
    }

    public void changePreference(String key, long newValue) {
        prefs.edit().putLong(key, newValue).apply();
    }

    public void changePreference(String key, int newValue) {
        prefs.edit().putInt(key, newValue).apply();
    }

    public void changePreference(String key, boolean newValue) {
        prefs.edit().putBoolean(key, newValue).apply();
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public long getLong(String key) {
        return getLong(key, -1);
    }

    public long getLong(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }

    public int getInt(String key) {
        return getInt(key, -1);
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }
}
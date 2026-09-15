package com.pafez.flashnote;

import android.content.Context;
import android.content.SharedPreferences;

public class GeminiSettings {

    private static final String PREFS_NAME = "gemini_settings";
    private static final String API_KEY = "api_key";

    private final SharedPreferences preferences;

    public GeminiSettings(Context context) {
        preferences = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    public void saveApiKey(String apiKey) {
        preferences.edit()
                .putString(API_KEY, apiKey)
                .apply();
    }

    public String getApiKey() {
        return preferences.getString(API_KEY, "");
    }

    public void clearApiKey() {
        preferences.edit()
                .remove(API_KEY)
                .apply();
    }

    public boolean hasApiKey() {
        return !getApiKey().isEmpty();
    }
}
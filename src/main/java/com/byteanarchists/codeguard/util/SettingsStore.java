package com.byteanarchists.codeguard.util;

import java.util.prefs.Preferences;

public class SettingsStore {
    private static final Preferences prefs = Preferences.userNodeForPackage(SettingsStore.class);
    private static final String AUTO_SAVE_KEY = "autoSave";
    private static final String HIGHLIGHT_KEY = "highlight";

    public static boolean isAutoSaveEnabled() {
        return prefs.getBoolean(AUTO_SAVE_KEY, false);
    }

    public static void setAutoSaveEnabled(boolean enabled) {
        prefs.putBoolean(AUTO_SAVE_KEY, enabled);
    }

    public static boolean isHighlightEnabled() {
        return prefs.getBoolean(HIGHLIGHT_KEY, true);
    }

    public static void setHighlightEnabled(boolean enabled) {
        prefs.putBoolean(HIGHLIGHT_KEY, enabled);
    }
}
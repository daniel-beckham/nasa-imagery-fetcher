package com.dsbeckham.nasaimageryfetcher.util;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.dsbeckham.nasaimageryfetcher.fragment.SettingsFragment;

import java.util.Map;

public class PreferenceUtils {
    public static final String PREF_NOTIFICATIONS = "pref_notifications";
    public static final String PREF_FETCH_CATEGORIES = "pref_fetch_categories";
    public static final String PREF_AUTOMATIC_DOWNLOADS = "pref_automatic_downloads";
    public static final String PREF_WIFI_DOWNLOADS_ONLY = "pref_wifi_downloads_only";
    public static final String PREF_CATEGORY_CYCLE_INTERVAL = "pref_category_cycle_interval";
    public static final String PREF_DOWNLOADED_IMAGE_CYCLE_INTERVAL = "pref_downloaded_image_cycle_interval";

    public static final String PREF_CURRENT_FRAGMENT = "pref_current_fragment";
    public static final String PREF_LAST_IOTD_DATE = "pref_last_iotd_date";
    public static final String PREF_LAST_APOD_DATE = "pref_last_apod_date";

    public static final String CATEGORY_IOTD = "category_iotd";
    public static final String CATEGORY_APOD = "category_apod";
    public static final String CATEGORY_BOTH = "category_both";

    public static final String FRAGMENT_IOTD = "fragment_iotd";
    public static final String FRAGMENT_APOD = "fragment_apod";
    public static final String FRAGMENT_SETTINGS = "fragment_settings";

    public static void togglePreferencesBasedOnAllCurrentKeyValues(Activity activity) {
        Map<String, ?> preferenceMap = PreferenceManager.getDefaultSharedPreferences(activity).getAll();

        for (Map.Entry<String, ?> entry : preferenceMap.entrySet()) {
            togglePreferencesBasedOnCurrentKeyValue(activity, PreferenceManager.getDefaultSharedPreferences(activity), entry.getKey());
        }
    }

    public static void togglePreferencesBasedOnCurrentKeyValue(Activity activity, SharedPreferences sharedPreferences, String key) {
        final SettingsFragment settingsFragment = (SettingsFragment) activity.getFragmentManager().findFragmentByTag(FRAGMENT_SETTINGS);

        if (settingsFragment == null) {
            return;
        }

        switch (key) {
            case PREF_AUTOMATIC_DOWNLOADS:
                if (sharedPreferences.getBoolean(key, false)) {
                    if (PermissionUtils.isStoragePermissionGranted(activity)) {
                        settingsFragment.findPreference(PREF_WIFI_DOWNLOADS_ONLY).setEnabled(true);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            settingsFragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    }
                } else {
                    settingsFragment.findPreference(PREF_WIFI_DOWNLOADS_ONLY).setEnabled(false);
                }
                break;
        }
    }
}

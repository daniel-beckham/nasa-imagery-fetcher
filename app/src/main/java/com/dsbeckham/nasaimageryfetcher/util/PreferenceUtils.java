package com.dsbeckham.nasaimageryfetcher.util;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.Preference;
import android.widget.Toast;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.fragment.SettingsFragment;

import java.io.File;
import java.util.Map;

public class PreferenceUtils {
    public static final String PREF_NOTIFICATIONS = "pref_notifications";
    public static final String PREF_FETCH_CATEGORIES = "pref_fetch_categories";
    public static final String PREF_SAVE_TO_EXTERNAL_STORAGE = "pref_save_to_external_storage";
    public static final String PREF_SET_AS_WALLPAPER = "pref_set_as_wallpaper";
    public static final String PREF_WALLPAPER_CATEGORY = "pref_wallpaper_category";
    public static final String PREF_WIFI_DOWNLOADS_ONLY = "pref_wifi_downloads_only";

    private static final String PREF_CLEAR_CACHED_IMAGES = "pref_clear_cached_images";
    private static final String PREF_CLEAR_SAVED_IMAGES = "pref_clear_saved_images";

    public static final String PREF_CURRENT_FRAGMENT = "pref_current_fragment";

    public static final String PREF_LAST_IOTD_DATE = "pref_last_iotd_date";
    public static final String PREF_LAST_APOD_DATE = "pref_last_apod_date";

    public static final String CATEGORY_IOTD = "category_iotd";
    public static final String CATEGORY_APOD = "category_apod";
    public static final String CATEGORY_BOTH = "category_both";

    public static final String FRAGMENT_IOTD = "fragment_iotd";
    public static final String FRAGMENT_APOD = "fragment_apod";
    public static final String FRAGMENT_SETTINGS = "fragment_settings";

    public static void processChangedPreference(Activity activity, SharedPreferences sharedPreferences, String key) {
        final SettingsFragment settingsFragment = (SettingsFragment) activity.getFragmentManager().findFragmentByTag(FRAGMENT_SETTINGS);

        if (settingsFragment == null) {
            return;
        }

        switch (key) {
            case PREF_NOTIFICATIONS:
            case PREF_SAVE_TO_EXTERNAL_STORAGE:
            case PREF_SET_AS_WALLPAPER:
                if (key.equals(PREF_SAVE_TO_EXTERNAL_STORAGE)) {
                    if (sharedPreferences.getBoolean(key, false)
                            && !PermissionUtils.isStoragePermissionGranted(activity)
                            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        settingsFragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                } else if (key.equals(PREF_SET_AS_WALLPAPER)) {
                    togglePreferencesBasedOnCurrentKeyValue(activity, sharedPreferences, key);
                }

                if (!sharedPreferences.getBoolean(PREF_NOTIFICATIONS, false)
                        && !sharedPreferences.getBoolean(PREF_SAVE_TO_EXTERNAL_STORAGE, false)
                        && !sharedPreferences.getBoolean(PREF_SET_AS_WALLPAPER, false)) {
                    AlarmUtils.cancelAlarm(activity);
                } else {
                    AlarmUtils.scheduleAlarm(activity);
                }
                break;
        }
    }

    public static void setUpPreferences(final Activity activity, SharedPreferences sharedPreferences) {
        final SettingsFragment settingsFragment = (SettingsFragment) activity.getFragmentManager().findFragmentByTag(FRAGMENT_SETTINGS);

        if (settingsFragment == null) {
            return;
        }

        settingsFragment.findPreference(PREF_CLEAR_CACHED_IMAGES).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FileUtils.deleteDirectoryTree(new File(activity.getCacheDir().getPath() + "/picasso-cache"));
                Toast.makeText(activity, activity.getString(R.string.cached_images_cleared), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        settingsFragment.findPreference(PREF_CLEAR_SAVED_IMAGES).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!PermissionUtils.isStoragePermissionGranted(activity)
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    settingsFragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE + 1);
                } else {
                    FileUtils.deleteDirectoryTree(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/" + DownloadUtils.IMAGE_DIRECTORY));
                    Toast.makeText(activity, activity.getString(R.string.saved_images_cleared), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });


        Map<String, ?> preferenceMap = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : preferenceMap.entrySet()) {
            togglePreferencesBasedOnCurrentKeyValue(activity, sharedPreferences, entry.getKey());
        }
    }

    private static void togglePreferencesBasedOnCurrentKeyValue(Activity activity, SharedPreferences sharedPreferences, String key) {
        final SettingsFragment settingsFragment = (SettingsFragment) activity.getFragmentManager().findFragmentByTag(FRAGMENT_SETTINGS);

        if (settingsFragment == null) {
            return;
        }

        switch (key) {
            case PREF_SET_AS_WALLPAPER:
                if (sharedPreferences.getBoolean(key, false)) {
                    settingsFragment.findPreference(PREF_WALLPAPER_CATEGORY).setEnabled(true);
                } else {
                    settingsFragment.findPreference(PREF_WALLPAPER_CATEGORY).setEnabled(false);
                }
                break;
        }
    }
}

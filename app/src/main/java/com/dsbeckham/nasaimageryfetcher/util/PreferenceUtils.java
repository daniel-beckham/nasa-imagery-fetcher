package com.dsbeckham.nasaimageryfetcher.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.fragment.SettingsFragment;
import com.dsbeckham.nasaimageryfetcher.fragment.TimePickerFragment;

import java.util.Calendar;
import java.util.Map;

public class PreferenceUtils {
    public static final String PREF_NOTIFICATIONS = "pref_notifications";
    public static final String PREF_AUTOMATIC_DOWNLOADS = "pref_automatic_downloads";
    public static final String PREF_WIFI_DOWNLOADS_ONLY = "pref_wifi_downloads_only";
    public static final String PREF_FETCH_CATEGORIES = "pref_fetch_categories";
    public static final String PREF_FETCH_TIME = "pref_fetch_time";
    public static final String PREF_AUTOMATIC_UPDATES = "pref_automatic_updates";
    public static final String PREF_DOWNLOADED_IMAGE_CYCLE_INTERVAL = "pref_downloaded_image_cycle_interval";
    public static final String PREF_DISPLAY_CATEGORIES = "pref_display_categories";
    public static final String PREF_CATEGORY_PRIORITY = "pref_category_priority";
    public static final String PREF_CATEGORY_CYCLE_INTERVAL = "pref_category_cycle_interval";
    public static final String PREF_CURRENT_FRAGMENT = "pref_current_fragment";

    public static void configureFetchTimePreference(final Activity activity) {
        final SettingsFragment settingsFragment = (SettingsFragment) activity.getFragmentManager().findFragmentByTag("settings");

        if (settingsFragment == null) {
            return;
        }

        final Preference fetchTimePreference = settingsFragment.findPreference(PREF_FETCH_TIME);

        if (fetchTimePreference != null) {
            fetchTimePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (preference.getKey().equals(PREF_FETCH_TIME)) {
                        TimePickerFragment timePickerFragment = new TimePickerFragment();
                        timePickerFragment.setPreference(preference);
                        timePickerFragment.setOnSharedPreferenceChangeListener(settingsFragment);
                        timePickerFragment.show(activity.getFragmentManager(), "timePicker");
                    }
                    return false;
                }
            });

            Calendar calendar = DateTimeUtils.getAssociatedPreferenceCalendar(activity, fetchTimePreference);
            int hour = calendar.get(Calendar.HOUR_OF_DAY), minute = calendar.get(Calendar.MINUTE);
            String time = DateTimeUtils.formatTime(activity, hour, minute);

            fetchTimePreference.setSummary(time);
        }
    }

    public static void setDefaultValuesForPreferences(Activity activity) {
        String time = PreferenceManager.getDefaultSharedPreferences(activity).getString(PREF_FETCH_TIME, "");

        if (time.isEmpty()) {
            PreferenceManager.getDefaultSharedPreferences(activity).edit().putString(PREF_FETCH_TIME, "12:00").apply();
        }

        PreferenceManager.setDefaultValues(activity, R.xml.preferences, false);
    }

    public static void togglePreferencesBasedOnAllCurrentKeyValues(Activity activity)  {
        Map<String, ?> preferenceMap = PreferenceManager.getDefaultSharedPreferences(activity).getAll();
        for (Map.Entry<String, ?> entry: preferenceMap.entrySet()) {
            togglePreferencesBasedOnCurrentKeyValue(activity, PreferenceManager.getDefaultSharedPreferences(activity), entry.getKey());
        }
    }

    public static void togglePreferencesBasedOnCurrentKeyValue(Activity activity, SharedPreferences sharedPreferences, String key) {
        final SettingsFragment settingsFragment = (SettingsFragment) activity.getFragmentManager().findFragmentByTag("settings");

        if (settingsFragment == null) {
            return;
        }

        switch (key) {
            case PREF_AUTOMATIC_DOWNLOADS:
                if (sharedPreferences.getBoolean(key, false)) {
                    settingsFragment.findPreference(PREF_WIFI_DOWNLOADS_ONLY).setEnabled(true);
                } else {
                    settingsFragment.findPreference(PREF_WIFI_DOWNLOADS_ONLY).setEnabled(false);
                }
                break;
            case PREF_DISPLAY_CATEGORIES:
                switch (sharedPreferences.getString(key, "")) {
                    case "iotd":
                    case "apod":
                        settingsFragment.findPreference(PREF_CATEGORY_PRIORITY).setEnabled(false);
                        settingsFragment.findPreference(PREF_CATEGORY_CYCLE_INTERVAL).setEnabled(false);
                        break;
                    case "iotd_apod":
                        settingsFragment.findPreference(PREF_CATEGORY_PRIORITY).setEnabled(true);
                        settingsFragment.findPreference(PREF_CATEGORY_CYCLE_INTERVAL).setEnabled(true);
                        break;
                }
                break;
        }
    }
}

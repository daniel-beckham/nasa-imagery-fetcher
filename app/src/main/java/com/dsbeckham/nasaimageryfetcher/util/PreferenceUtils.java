package com.dsbeckham.nasaimageryfetcher.util;

import android.app.Activity;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.fragment.SettingsFragment;
import com.dsbeckham.nasaimageryfetcher.fragment.TimePickerFragment;

import java.util.Calendar;
import java.util.Map;

public class PreferenceUtils {
    public static final String PREF_FETCH_MODE = "pref_fetch_mode";
    public static final String PREF_FETCH_CATEGORIES = "pref_fetch_categories";
    public static final String PREF_AUTOMATIC_UPDATES = "pref_automatic_updates";
    public static final String PREF_WIFI_UPDATES_ONLY = "pref_wifi_updates_only";
    public static final String PREF_UPDATE_TIME = "pref_update_time";
    public static final String PREF_RANDOM_IMAGES_UPDATE_FREQUENCY = "pref_update_frequency";
    public static final String PREF_DISPLAY_CATEGORIES = "pref_display_categories";
    public static final String PREF_CATEGORY_CYCLE_INTERVAL = "pref_category_cycle_interval";
    public static final String PREF_CACHED_IMAGES_ONLY = "pref_cached_images_only";
    public static final String PREF_CACHED_IMAGES_CYCLE_INTERVAL = "pref_cached_images_cycle_interval";
    public static final String PREF_SEND_NOTIFICATIONS = "pref_send_notifications";

    public static final String PREF_CURRENT_FRAGMENT = "pref_current_fragment";

    public static final int PREF_FETCH_MODE_DAILY_IMAGES = 0;
    public static final int PREF_FETCH_MODE_RANDOM_IMAGES = 1;

    public static final int PREF_CATEGORIES_IOTD_APOD = 0;
    public static final int PREF_CATEGORIES_IOTD = 1;
    public static final int PREF_CATEGORIES_APOD = 2;

    public static void setDefaultValuesForPreferences(Activity activity) {
        String time = PreferenceManager.getDefaultSharedPreferences(activity).getString(PREF_UPDATE_TIME, "");

        if (time.isEmpty()) {
            PreferenceManager.getDefaultSharedPreferences(activity).edit().putString(PREF_UPDATE_TIME, "12:00").apply();
        }

        PreferenceManager.setDefaultValues(activity, R.xml.preferences, false);
    }

    public static void setUpPreferences(final Activity activity) {
        final SettingsFragment settingsFragment = (SettingsFragment) activity.getFragmentManager().findFragmentByTag("settings");

        if (settingsFragment == null) {
            return;
        }

        final Preference updateTimePreference = settingsFragment.findPreference(PREF_UPDATE_TIME);

        if (updateTimePreference != null) {
            updateTimePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                  @Override
                  public boolean onPreferenceClick(Preference preference) {
                      if (preference.getKey().equals(PREF_UPDATE_TIME)) {
                          TimePickerFragment timePickerFragment = new TimePickerFragment();
                          timePickerFragment.setPreference(preference);
                          timePickerFragment.setOnSharedPreferenceChangeListener(settingsFragment);
                          timePickerFragment.show(activity.getFragmentManager(), "timePicker");
                      }
                      return false;
                  }
            });

            Calendar calendar = DateTimeUtils.getAssociatedPreferenceCalendar(activity, updateTimePreference);
            int hour = calendar.get(Calendar.HOUR_OF_DAY), minute = calendar.get(Calendar.MINUTE);
            String time = DateTimeUtils.formatTime(activity, hour, minute);

            updateTimePreference.setSummary(time);
        }

        Map<String, ?> preferenceMap = PreferenceManager.getDefaultSharedPreferences(activity).getAll();
        for (Map.Entry<String, ?> entry: preferenceMap.entrySet()) {
            togglePreferencesBasedOnCurrentKeyValue(activity, entry.getKey());
        }
    }

    public static void togglePreferencesBasedOnCurrentKeyValue(Activity activity, String key) {
        final SettingsFragment settingsFragment = (SettingsFragment) activity.getFragmentManager().findFragmentByTag("settings");

        if (settingsFragment == null) {
            return;
        }

        switch (key) {
            case PREF_FETCH_MODE:
                switch (Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(activity).getString(PREF_FETCH_MODE, ""))) {
                    case PREF_FETCH_MODE_DAILY_IMAGES:
                        settingsFragment.findPreference(PREF_UPDATE_TIME).setEnabled(true);
                        settingsFragment.findPreference(PREF_RANDOM_IMAGES_UPDATE_FREQUENCY).setEnabled(false);
                        break;
                    case PREF_FETCH_MODE_RANDOM_IMAGES:
                        settingsFragment.findPreference(PREF_UPDATE_TIME).setEnabled(false);
                        settingsFragment.findPreference(PREF_RANDOM_IMAGES_UPDATE_FREQUENCY).setEnabled(true);
                        break;
                }
                break;
            case PREF_AUTOMATIC_UPDATES:
                if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(PREF_AUTOMATIC_UPDATES, false)) {
                    settingsFragment.findPreference(PREF_WIFI_UPDATES_ONLY).setEnabled(true);
                } else {
                    settingsFragment.findPreference(PREF_WIFI_UPDATES_ONLY).setEnabled(false);
                }
                break;
            case PREF_DISPLAY_CATEGORIES:
                switch (Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(activity).getString(PREF_DISPLAY_CATEGORIES, ""))) {
                    case PREF_CATEGORIES_APOD:
                    case PREF_CATEGORIES_IOTD:
                        settingsFragment.findPreference(PREF_CATEGORY_CYCLE_INTERVAL).setEnabled(false);
                        break;
                    case PREF_CATEGORIES_IOTD_APOD:
                        settingsFragment.findPreference(PREF_CATEGORY_CYCLE_INTERVAL).setEnabled(true);
                        break;
                }
                break;
            case PREF_CACHED_IMAGES_ONLY:
                if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(PREF_CACHED_IMAGES_ONLY, false)) {
                    settingsFragment.findPreference(PREF_CACHED_IMAGES_CYCLE_INTERVAL).setEnabled(true);
                } else {
                    settingsFragment.findPreference(PREF_CACHED_IMAGES_CYCLE_INTERVAL).setEnabled(false);
                }
                break;
        }
    }
}

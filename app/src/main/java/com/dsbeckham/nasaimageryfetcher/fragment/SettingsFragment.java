package com.dsbeckham.nasaimageryfetcher.fragment;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.util.PreferenceUtils;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceUtils.setUpPreferences(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PreferenceUtils.togglePreferencesBasedOnCurrentKeyValue(getActivity(), key);
    }
}

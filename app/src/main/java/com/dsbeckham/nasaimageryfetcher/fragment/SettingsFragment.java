package com.dsbeckham.nasaimageryfetcher.fragment;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.util.PermissionUtils;
import com.dsbeckham.nasaimageryfetcher.util.PreferenceUtils;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceUtils.togglePreferencesBasedOnAllCurrentKeyValues(getActivity(), PreferenceManager.getDefaultSharedPreferences(getActivity()));
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PreferenceUtils.processPreference(getActivity(), sharedPreferences, key);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PreferenceUtils.togglePreferencesBasedOnCurrentKeyValue(getActivity(), PreferenceManager.getDefaultSharedPreferences(getActivity()), PreferenceUtils.PREF_AUTOMATIC_DOWNLOADS);
                } else {
                    ((SwitchPreference) findPreference(PreferenceUtils.PREF_AUTOMATIC_DOWNLOADS)).setChecked(false);
                }
                break;
        }
    }
}

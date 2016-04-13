package com.dsbeckham.nasaimageryfetcher.util;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;

public class ListPreferenceHelper extends ListPreference {
    public ListPreferenceHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ListPreferenceHelper(Context context) {
        super(context);
        init();
    }

    private void init() {
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(getEntry());
                return true;
            }
        });
    }

    @Override
    public CharSequence getSummary() {
        return super.getEntry();
    }
}

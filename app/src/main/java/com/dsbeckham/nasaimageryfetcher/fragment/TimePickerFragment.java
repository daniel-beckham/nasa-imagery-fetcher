package com.dsbeckham.nasaimageryfetcher.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.TimePicker;

import com.dsbeckham.nasaimageryfetcher.util.DateTimeUtils;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;
    private Preference preference;
    private SharedPreferences sharedPreferences;

    public void setPreference(Preference preference) {
        this.preference = preference;
    }

    public void setOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        this.onSharedPreferenceChangeListener = onSharedPreferenceChangeListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = DateTimeUtils.getAssociatedPreferenceCalendar(getActivity(), preference);
        int hour = calendar.get(Calendar.HOUR_OF_DAY), minute = calendar.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), this, hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hour, int minute) {
        String time = DateTimeUtils.formatTime(getActivity(), hour, minute);
        preference.setSummary(time);
        sharedPreferences.edit().putString(preference.getKey(), time).apply();
        onSharedPreferenceChangeListener.onSharedPreferenceChanged(sharedPreferences, preference.getKey());
    }
}

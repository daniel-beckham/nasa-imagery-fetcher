package com.dsbeckham.nasaimageryfetcher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.dsbeckham.nasaimageryfetcher.util.AlarmUtils;
import com.dsbeckham.nasaimageryfetcher.util.PreferenceUtils;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceUtils.PREF_NOTIFICATIONS, false)
                || PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceUtils.PREF_SAVE_TO_EXTERNAL_STORAGE, false)
                || PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceUtils.PREF_SET_AS_WALLPAPER, false)) {
            AlarmUtils.scheduleAlarm(context);
        }
    }
}

package com.dsbeckham.nasaimageryfetcher.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.dsbeckham.nasaimageryfetcher.service.BackgroundService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        scheduleAlarm(context);
    }

    public static void scheduleAlarm(Context context) {
        Intent intent = new Intent(context, BackgroundService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_DAY, AlarmManager.INTERVAL_HALF_DAY, pendingIntent);
    }
}

package com.dsbeckham.nasaimageryfetcher.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.dsbeckham.nasaimageryfetcher.service.BackgroundService;

public class AlarmUtils {
    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, BackgroundService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public static void scheduleAlarm(Context context) {
        Intent intent = new Intent(context, BackgroundService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_DAY, AlarmManager.INTERVAL_HALF_DAY, pendingIntent);
    }
}

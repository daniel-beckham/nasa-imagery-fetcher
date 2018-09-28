package com.beckhamd.nasaimageryfetcher.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.beckhamd.nasaimageryfetcher.R;
import com.beckhamd.nasaimageryfetcher.activity.InformationActivity;
import com.beckhamd.nasaimageryfetcher.job.BackgroundJob;
import com.beckhamd.nasaimageryfetcher.model.UniversalImageModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationUtils {
    private static final String NOTIFICATION_GROUP = "com.beckhamd.nasaimageryfetcher.notification.GROUP";
    private static final int NOTIFICATION_GROUP_ID = 0;

    public static void createNotification(Context context, ArrayList<UniversalImageModel> models) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(BackgroundJob.TAG, context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW);
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        List<Notification> notifications = new ArrayList<>();

        for (UniversalImageModel model : models) {
            String category = "";

            switch (model.getType()) {
                case ModelUtils.MODEL_TYPE_IOTD:
                    category = context.getString(R.string.app_iotd);
                    break;
                case ModelUtils.MODEL_TYPE_APOD:
                    category = context.getString(R.string.app_apod);
                    break;
            }

            Intent intent = new Intent(context, InformationActivity.class)
                .putParcelableArrayListExtra(InformationActivity.EXTRA_MODELS, new ArrayList<>(Collections.singletonList(model)))
                .putExtra(InformationActivity.EXTRA_POSITION, 0)
                .putExtra(InformationActivity.EXTRA_TYPE, InformationActivity.EXTRA_TYPE_MIXED)
                .setAction(Long.toString(System.currentTimeMillis()));

            PendingIntent pendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(intent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(context, BackgroundJob.TAG)
                    .setSmallIcon(R.mipmap.ic_notification)
                    .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                    .setContentIntent(pendingIntent)
                    .setContentTitle(category)
                    .setContentText(model.getTitle())
                    .setGroup(NOTIFICATION_GROUP)
                    .setAutoCancel(true)
                    .build();

            notifications.add(notification);
        }

        int notificationId = 1;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        for (Notification notification : notifications) {
            notificationManager.notify(notificationId++, notification);
        }

        if (models.size() > 1) {
            Intent intent = new Intent(context, InformationActivity.class)
                    .putParcelableArrayListExtra(InformationActivity.EXTRA_MODELS, models)
                    .putExtra(InformationActivity.EXTRA_POSITION, 0)
                    .putExtra(InformationActivity.EXTRA_TYPE, InformationActivity.EXTRA_TYPE_MIXED)
                    .setAction(Long.toString(System.currentTimeMillis()));

            PendingIntent pendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(intent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification summaryNotification = new NotificationCompat.Builder(context, BackgroundJob.TAG)
                    .setSmallIcon(R.mipmap.ic_notification)
                    .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                    .setContentIntent(pendingIntent)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.notification_summary, models.size()))
                    .setGroup(NOTIFICATION_GROUP)
                    .setGroupSummary(true)
                    .setAutoCancel(true)
                    .build();

            notificationManager.notify(NOTIFICATION_GROUP_ID, summaryNotification);
        }
    }
}

package com.dsbeckham.nasaimageryfetcher.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.activity.InformationActivity;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.util.ApodQueryUtils;
import com.dsbeckham.nasaimageryfetcher.util.DownloadUtils;
import com.dsbeckham.nasaimageryfetcher.util.IotdQueryUtils;
import com.dsbeckham.nasaimageryfetcher.util.ModelUtils;
import com.dsbeckham.nasaimageryfetcher.util.PermissionUtils;
import com.dsbeckham.nasaimageryfetcher.util.PreferenceUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class BackgroundService extends IntentService {
    private List<UniversalImageModel> models = new ArrayList<>();

    public BackgroundService() {
        super("BackgroundService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceUtils.PREF_NOTIFICATIONS, false)
                || PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceUtils.PREF_AUTOMATIC_DOWNLOADS, false)) {
            models.clear();

            String fetchCategories = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceUtils.PREF_FETCH_CATEGORIES, "");

            if (fetchCategories.equals(PreferenceUtils.CATEGORY_IOTD) || fetchCategories.equals(PreferenceUtils.CATEGORY_BOTH)) {
                IotdQueryUtils.setUpIoService();
                IotdQueryUtils.getLatestImage(this);
            }

            if (fetchCategories.equals(PreferenceUtils.CATEGORY_APOD) || fetchCategories.equals(PreferenceUtils.CATEGORY_BOTH)) {
                ApodQueryUtils.setUpIoServices();
                ApodQueryUtils.getLatestImage(this, false);
            }
        }
    }

    public void processLatestImage(UniversalImageModel universalImageModel) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceUtils.PREF_NOTIFICATIONS, false)) {
            models.add(universalImageModel);

            Intent intent = new Intent(this, InformationActivity.class);
            intent.putExtra(InformationActivity.EXTRA_MODELS, Parcels.wrap(models));
            intent.putExtra(InformationActivity.EXTRA_POSITION, 0);
            intent.putExtra(InformationActivity.EXTRA_TYPE, InformationActivity.EXTRA_TYPE_MIXED);
            intent.setAction(Long.toString(System.currentTimeMillis()));

            PendingIntent pendingIntent = TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(intent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setWhen(System.currentTimeMillis());

            if (models.size() > 1) {
                NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(getString(R.string.notification_title, models.size()))
                        .setSummaryText(getString(R.string.app_name));

                StringBuilder stringBuilder = new StringBuilder();

                for (UniversalImageModel model : models) {
                    String category = "";

                    switch (model.getType()) {
                        case ModelUtils.MODEL_TYPE_IOTD:
                            category = getString(R.string.app_iotd);
                            break;
                        case ModelUtils.MODEL_TYPE_APOD:
                            category = getString(R.string.app_apod);
                            break;
                    }

                    stringBuilder.append(getString(R.string.notification_content, category, model.getTitle()));
                }

                String bigText = stringBuilder.toString()
                        .replaceAll("(<br/>)+$", "");
                bigTextStyle.bigText(Html.fromHtml(bigText));

                builder.setNumber(models.size())
                        .setStyle(bigTextStyle);
            } else {
                String category = getString(R.string.app_name);

                switch (universalImageModel.getType()) {
                    case ModelUtils.MODEL_TYPE_IOTD:
                        category = getString(R.string.app_iotd);
                        break;
                    case ModelUtils.MODEL_TYPE_APOD:
                        category = getString(R.string.app_apod);
                        break;
                }

                builder.setContentTitle(category)
                        .setContentText(universalImageModel.getTitle());
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder.build());
        }

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceUtils.PREF_AUTOMATIC_DOWNLOADS, false)) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceUtils.PREF_WIFI_DOWNLOADS_ONLY, false)) {
                boolean wifiConnected = false;
                ConnectivityManager connectivityManager = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

                if (connectivityManager != null) {
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                    if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
                        wifiConnected = true;
                    }
                }

                if (!wifiConnected) {
                    return;
                }
            }

            if (PermissionUtils.isStoragePermissionGranted(this)) {
                DownloadUtils.validateAndDownloadImage(this, universalImageModel, true, false);
            }
        }
    }
}

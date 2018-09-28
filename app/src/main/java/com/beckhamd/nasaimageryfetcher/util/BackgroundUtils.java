package com.beckhamd.nasaimageryfetcher.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.beckhamd.nasaimageryfetcher.model.UniversalImageModel;

import java.util.ArrayList;

public class BackgroundUtils {
    private static final ArrayList<UniversalImageModel> models = new ArrayList<>();

    public static void getLatestImages(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceUtils.PREF_NOTIFICATIONS, false)
                || PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceUtils.PREF_SAVE_TO_EXTERNAL_STORAGE, false)
                || PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceUtils.PREF_SET_AS_WALLPAPER, false)) {
            models.clear();

            String fetchCategories = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceUtils.PREF_FETCH_CATEGORIES, "");

            if (fetchCategories != null && !fetchCategories.isEmpty()) {
                if (fetchCategories.equals(PreferenceUtils.CATEGORY_IOTD)
                        || fetchCategories.equals(PreferenceUtils.CATEGORY_BOTH)) {
                    IotdQueryUtils.setUpIoService(context);
                    IotdQueryUtils.getLatestImage(context);
                }

                if (fetchCategories.equals(PreferenceUtils.CATEGORY_APOD)
                        || fetchCategories.equals(PreferenceUtils.CATEGORY_BOTH)) {
                    ApodQueryUtils.setUpIoServices(context);
                    ApodQueryUtils.getLatestImage(context, false);
                }
            }

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceUtils.PREF_NOTIFICATIONS, false)
                    && !models.isEmpty()) {
                NotificationUtils.createNotification(context, models);
            }
        }
    }

    public static void processLatestImage(Context context, UniversalImageModel universalImageModel) {
        models.add(universalImageModel);

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceUtils.PREF_WIFI_DOWNLOADS_ONLY, false)) {
            boolean wifiConnected = false;
            ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));

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

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceUtils.PREF_SAVE_TO_EXTERNAL_STORAGE, false)) {
            if (PermissionUtils.isStoragePermissionGranted(context)) {
                DownloadUtils.validateAndDownloadImage(context, universalImageModel, true, false, false);
            }
        }

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceUtils.PREF_SET_AS_WALLPAPER, false)) {
            String wallpaperCategory = PreferenceManager.getDefaultSharedPreferences(context).getString(PreferenceUtils.PREF_WALLPAPER_CATEGORY, "");

            if (wallpaperCategory != null && !wallpaperCategory.isEmpty()) {
                if ((wallpaperCategory.equals(PreferenceUtils.CATEGORY_IOTD) && universalImageModel.getType().equals(ModelUtils.MODEL_TYPE_IOTD))
                        || (wallpaperCategory.equals(PreferenceUtils.CATEGORY_APOD) && universalImageModel.getType().equals(ModelUtils.MODEL_TYPE_APOD))) {
                    WallpaperUtils.setWallpaper(context, universalImageModel.getImageUrl(), false);
                }
            }
        }
    }
}

package com.beckhamd.nasaimageryfetcher.util;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.beckhamd.nasaimageryfetcher.model.UniversalImageModel;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtils {
    public static final String IMAGE_DIRECTORY = "NASAImageryFetcher";

    public static void validateAndDownloadImage(final Context context, final UniversalImageModel universalImageModel, final boolean hidden,
                                                final boolean fallback, final boolean background) {
        if (background) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    validateImage(context, universalImageModel, hidden, fallback);
                }
            }).start();
        } else {
            validateImage(context, universalImageModel, hidden, fallback);
        }
    }

    private static void validateImage(final Context context, final UniversalImageModel universalImageModel, final boolean hidden, final boolean fallback) {
        try {
            String url = fallback ? universalImageModel.getImageThumbnailUrl() : universalImageModel.getImageUrl();
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setRequestMethod("HEAD");

            switch (httpURLConnection.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    if (!fallback) {
                        downloadImage(context, universalImageModel, hidden, false);
                    } else {
                        downloadImage(context, universalImageModel, hidden, true);
                    }
                    break;
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_MOVED_PERM:
                    HttpURLConnection httpURLMovedConnection = (HttpURLConnection) new URL(httpURLConnection.getHeaderField("Location")).openConnection();
                    httpURLMovedConnection.setInstanceFollowRedirects(false);
                    httpURLMovedConnection.setRequestMethod("HEAD");

                    if (httpURLMovedConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        if (!fallback) {
                            downloadImage(context, universalImageModel, hidden, false);
                        } else {
                            downloadImage(context, universalImageModel, hidden, true);
                        }
                    }
                    break;
                default:
                    if (!fallback) {
                        validateImage(context, universalImageModel, hidden, true);
                    }
                    break;
            }
        } catch (Exception e) {
            if (!fallback) {
                validateImage(context, universalImageModel, hidden, true);
            }
        }
    }

    private static void downloadImage(Context context, UniversalImageModel universalImageModel, boolean hidden, boolean alternate) {
        String subDirectories = "/" + IMAGE_DIRECTORY + "/" + universalImageModel.getType();
        String downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + subDirectories;

        if (FileUtils.createDirectoryIfNotExists(new File(downloadDirectory))) {
            Uri uri = alternate ? Uri.parse(universalImageModel.getImageThumbnailUrl()) : Uri.parse(universalImageModel.getImageUrl());

            DownloadManager.Request request = new DownloadManager.Request(uri)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, subDirectories + "/" + uri.getLastPathSegment())
                    .setNotificationVisibility(hidden ? DownloadManager.Request.VISIBILITY_HIDDEN : DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setVisibleInDownloadsUi(false);

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
        }
    }
}

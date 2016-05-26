package com.dsbeckham.nasaimageryfetcher.util;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;

import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtils {
    private static final int DOWNLOAD_URI = 0;
    private static final int DOWNLOAD_URI_ALTERNATE = 1;
    private static final int VALIDATE_URI_ALTERNATE = 2;

    private static class MessageHandler extends Handler {
        private Context context;
        private UniversalImageModel universalImageModel;
        private boolean hidden;

        private MessageHandler(Context context, UniversalImageModel universalImageModel, boolean hidden) {
            this.context = context;
            this.universalImageModel = universalImageModel;
            this.hidden = hidden;
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case DOWNLOAD_URI:
                    downloadImage(context, universalImageModel, hidden, false);
                    break;
                case DOWNLOAD_URI_ALTERNATE:
                    downloadImage(context, universalImageModel, hidden, true);
                    break;
                case VALIDATE_URI_ALTERNATE:
                    validateAndDownloadImage(context, universalImageModel, hidden, true);
                    break;
            }
        }
    }

    private static void downloadImage(Context context, UniversalImageModel universalImageModel, boolean hidden, boolean alternate) {
        Uri uri = alternate ? Uri.parse(universalImageModel.getImageThumbnailUrl()) : Uri.parse(universalImageModel.getImageUrl());
        String subPath = "/NASAImageryFetcher/" + universalImageModel.getType() + "/" + uri.getLastPathSegment();
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri).setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, subPath)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, subPath)
                .setNotificationVisibility(hidden ? DownloadManager.Request.VISIBILITY_HIDDEN : DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
                .setVisibleInDownloadsUi(false);
        downloadManager.enqueue(request);
    }

    public static void validateAndDownloadImage(final Context context, final UniversalImageModel universalImageModel, boolean hidden, final boolean fallback) {
        final MessageHandler messageHandler = new MessageHandler(context, universalImageModel, hidden);

        new Thread() {
            @Override
            public void run() {
                try {
                    HttpURLConnection.setFollowRedirects(false);

                    HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(!fallback ? universalImageModel.getImageUrl() : universalImageModel.getImageThumbnailUrl()).openConnection();
                    httpURLConnection.setRequestMethod("HEAD");

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        if (!fallback) {
                            messageHandler.sendEmptyMessage(DOWNLOAD_URI);
                        } else {
                            messageHandler.sendEmptyMessage(DOWNLOAD_URI_ALTERNATE);
                        }
                    } else {
                        if (!fallback) {
                            messageHandler.sendEmptyMessage(VALIDATE_URI_ALTERNATE);
                        }
                    }
                }
                catch (Exception e) {
                    if (!fallback) {
                        messageHandler.sendEmptyMessage(VALIDATE_URI_ALTERNATE);
                    }
                }
            }
        }.start();
    }
}

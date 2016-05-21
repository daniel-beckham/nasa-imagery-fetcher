package com.dsbeckham.nasaimageryfetcher.util;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtils {
    private static final int DOWNLOAD_URI = 0;
    private static final int DOWNLOAD_URI_ALTERNATE = 1;
    private static final int VALIDATE_URI_ALTERNATE = 2;

    private static void downloadFile(Activity activity, Uri uri) {
        String subPath = "/NASAImageryFetcher/" + PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "") + "/" + uri.getLastPathSegment();
        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri).setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, subPath)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
                .setVisibleInDownloadsUi(false);
        downloadManager.enqueue(request);
    }

    private static class MessageHandler extends Handler {
        private Activity activity;
        private Uri uri;
        private Uri uriAlternate;

        private MessageHandler(Activity activity, Uri uri, Uri alternateUri) {
            this.activity = activity;
            this.uri = uri;
            this.uriAlternate = alternateUri;
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case DOWNLOAD_URI:
                    downloadFile(activity, uri);
                    break;
                case DOWNLOAD_URI_ALTERNATE:
                    downloadFile(activity, uriAlternate);
                    break;
                case VALIDATE_URI_ALTERNATE:
                    validateUriAndDownloadFile(activity, uri, uriAlternate, true);
                    break;
            }
        }
    }

    public static void validateUriAndDownloadFile(final Activity activity, final Uri uri, final Uri uriAlternate, final boolean fallback) {
        final MessageHandler messageHandler = new MessageHandler(activity, uri, uriAlternate);

        new Thread() {
            @Override
            public void run() {
                try {
                    HttpURLConnection.setFollowRedirects(false);

                    HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(!fallback ? uri.toString() : uriAlternate.toString()).openConnection();
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

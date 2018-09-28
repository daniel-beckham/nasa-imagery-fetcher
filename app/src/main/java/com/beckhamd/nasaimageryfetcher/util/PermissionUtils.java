package com.beckhamd.nasaimageryfetcher.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

public class PermissionUtils {
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    public static boolean isStoragePermissionGranted(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}

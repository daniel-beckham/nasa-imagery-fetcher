package com.dsbeckham.nasaimageryfetcher.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

public class PermissionUtils {
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    public static boolean isStoragePermissionGranted(Activity activity) {
        return Build.VERSION.SDK_INT < 23 || ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}

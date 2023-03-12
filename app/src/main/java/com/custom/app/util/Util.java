package com.custom.app.util;

import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.content.ContextCompat;

public class Util {

    private Util() {
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void showAppSettings(Activity activity, String packageName) {
        Intent intent = new Intent(ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
}
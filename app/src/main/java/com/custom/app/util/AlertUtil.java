package com.custom.app.util;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class AlertUtil {

    private AlertUtil() {
    }

    public static void showToast(Context context, CharSequence msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showActionAlertDialog(Context context, CharSequence msg,
                                             CharSequence negativeAction, CharSequence positiveAction,
                                             OnClickListener listener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setMessage(msg);
        alert.setCancelable(true);
        alert.setPositiveButton(positiveAction, listener);
        alert.setNegativeButton(negativeAction, (dialog, which) -> dialog.dismiss());
        alert.show();
    }
}
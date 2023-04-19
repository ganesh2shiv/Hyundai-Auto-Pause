package com.custom.app.util

import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

object AlertUtil {

    fun showToast(context: Context, msg: CharSequence?) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun showActionAlertDialog(
        context: Context, msg: CharSequence?,
        negativeAction: CharSequence?, positiveAction: CharSequence?,
        listener: DialogInterface.OnClickListener?
    ) {
        val alert = AlertDialog.Builder(context)
        alert.setMessage(msg)
        alert.setCancelable(true)
        alert.setPositiveButton(positiveAction, listener)
        alert.setNegativeButton(negativeAction) { dialog, _: Int -> dialog.dismiss() }
        alert.show()
    }
}
package com.example.amapusage.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.example.amapusage.R
import kotlinx.android.synthetic.main.layout_dialog.view.*

object DialogUtils {

    fun showDialog(context: Context, title: String, message: String) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.cancel, null)
            .show()
    }

    fun gpsNotifyDialog(context: Context) {
        val layout = LayoutInflater.from(context).inflate(R.layout.layout_dialog, null) as CardView
        val dialog: Dialog = AlertDialog.Builder(context, R.style.dialogNoBg).create()
        dialog.setCancelable(false)
        layout.negativeButton.setOnClickListener { dialog.dismiss() }
        layout.positiveButton.setOnClickListener {
            dialog.dismiss().also {
                context.startActivity(Intent().apply {
                    action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
        if (!(context as Activity).isFinishing) {
            dialog.show()
            dialog.window?.setContentView(layout)
        }
    }

    fun gpsPermissionDialog(context: Context) {
        val layout = LayoutInflater.from(context).inflate(R.layout.layout_dialog, null) as CardView
        val dialog: Dialog = AlertDialog.Builder(context, R.style.dialogNoBg).create()
        layout.title.text = context.getText(R.string.authorization_request)
        layout.message.text = context.getText(R.string.authorization_message)
        dialog.setCancelable(false)
        layout.negativeButton.setOnClickListener { dialog.dismiss() }
        layout.positiveButton.setOnClickListener {
            dialog.dismiss().also {
                context.startActivity(Intent().apply {
                    data = Uri.parse("package:" + context.packageName)
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                })
            }
        }
        if (!(context as Activity).isFinishing) {
            dialog.show()
            dialog.window?.setContentView(layout)
        }
    }
}
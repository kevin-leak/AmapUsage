package com.example.amapusage.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.example.amapusage.App

object ScreenUtils {
    fun setStatus(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 21) {//21表示5.0
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.or(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.window.statusBarColor = Color.TRANSPARENT;
        } else if (Build.VERSION.SDK_INT >= 19) {//19表示4.4
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
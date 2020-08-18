package com.example.amapusage.utils

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import java.lang.reflect.Field


object ScreenUtils {
    fun setStatus(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 21) {//21表示5.0
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.or(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.window.statusBarColor = Color.TRANSPARENT
        } else {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }//19表示4.4
    }

    //dip和px转换
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun setWhiteStatusBlackFont(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.statusBarColor =
                ContextCompat.getColor(activity, R.color.white) //设置状态栏颜色
            //实现状态栏图标和文字颜色为暗色
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            activity.window.statusBarColor = Color.TRANSPARENT
        }
    }

    fun getScreenHeight(context: Context): Int {
        val windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point.y
    }

    @SuppressLint("PrivateApi")
    fun getStatusBarHeight(context: Context): Int {
        var c: Class<*>? = null
        var obj: Any? = null
        var field: Field? = null
        var x = 0
        var sbar = 0
        try {
            c = Class.forName("com.android.internal.R\$dimen")
            obj = c.newInstance()
            field = c.getField("status_bar_height")
            x = field[obj].toString().toInt()
            sbar = context.resources.getDimensionPixelSize(x)
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
        return sbar
    }
}
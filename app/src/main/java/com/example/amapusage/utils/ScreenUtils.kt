package com.example.amapusage.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.example.amapusage.R
import java.lang.reflect.Method


object ScreenUtils {

    fun setStatus(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 21) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.window.statusBarColor = Color.TRANSPARENT
        } else {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun setWhiteStatusBlackFont(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //设置状态栏颜色
            activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.white)
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            activity.window.statusBarColor = Color.TRANSPARENT
        }
    }

    fun getScreenHeight(context: Context): Int {
        val windowManager: WindowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point.y
    }

    fun getStatusBarHeight(context: Context): Int {
        var sbar = 0
        try {
            val c: Class<*> = Class.forName("com.android.internal.R\$dimen")
            val obj: Any = c.newInstance()
            val field: Int = c.getField("status_bar_height")[obj].toString().toInt()
            sbar = context.resources.getDimensionPixelSize(field)
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
        return sbar
    }

    private fun getDpi(context: Context): Int {
        var dpi = 0
        val windowManager: WindowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        try {
            val c: Class<*> = Class.forName("android.view.Display")
            val method: Method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
            val displayMetrics = DisplayMetrics()
            method.invoke(windowManager.defaultDisplay, displayMetrics)
            dpi = displayMetrics.heightPixels
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return dpi
    }

    fun getNavigationBarHeight(context: Context): Int {
        val totalHeight = getDpi(context)
        val contentHeight = getScreenHeight(context)
        return totalHeight - contentHeight
    }

    fun setNavigationBarColor(activity: Activity, color: Int) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        activity.window.navigationBarColor = color
    }

}
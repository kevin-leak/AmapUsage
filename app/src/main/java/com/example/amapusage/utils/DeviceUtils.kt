package com.example.amapusage.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import java.lang.reflect.Field

object DeviceUtils {
    /**
     * 获取屏幕高度
     */
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
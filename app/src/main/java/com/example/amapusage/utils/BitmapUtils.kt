package com.example.amapusage.utils

import android.graphics.Bitmap


object BitmapUtils {
     fun createScaledBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val w = bm.width // 得到图片的宽，高
        val h = bm.height
        val retX: Int
        val retY: Int
        val wh = w.toDouble() / h.toDouble()
        val nwh = newWidth.toDouble() / newHeight.toDouble()
        if (wh > nwh) {
            retX = h * newWidth / newHeight
            retY = h
        } else {
            retX = w
            retY = w * newHeight / newWidth
        }
        val startX = if (w > retX) (w - retX) / 2 else 0 //基于原图，取正方形左上角x坐标
        val startY = if (h > retY) (h - retY) / 2 else 0
        val bit = Bitmap.createBitmap(bm, startX, startY, retX, retY, null, false)
        bm.recycle()
        return bit
    }
}
package com.example.amapusage.collapse

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.cos

class CollapseInterpolator : AccelerateDecelerateInterpolator() {

    override fun getInterpolation(input: Float): Float {
        return (cos((input + 1) * Math.PI) / 2.0f).toFloat() + 0.5f
    }


}
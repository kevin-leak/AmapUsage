package com.example.amapusage.collapse

import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation

interface ControlSensorPerformer {
    companion object {
        val BEFORE_COLLLAPSING = 1
        val ON_COLLLAPSING = 2
        val AFTER_COLLLAPSING = 3
    }

    interface Controller
    interface Sensor {

        fun isCollapsed(): Boolean

        fun animation()
        fun expand()
        fun collapsing()

        fun bindCollapsingView(view: View): Sensor // 默认第一个

        // 当view为空表示默认的linkage
        fun setLinkages(view: View? = null, linkage: Linkage, tag: Int = AFTER_COLLLAPSING): Sensor
        fun getLinkages(): MutableMap<View, Linkage?>
    }

    interface CollapsingListener {
        fun beforeCollapsingStateChange(sensor: Sensor)
        fun onCollapsingStateChange(sensor: Sensor)
        fun collapsingStateChanged(sensor: Sensor)
    }

    interface Linkage {
        var tag: Int
        fun action(view: View)
    }
}




package com.example.amapusage.collapse

import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation

interface IScrollSensor {
    interface Controller {
    }

    interface Sensor {
        fun isCollapsed(): Boolean
        fun bindCollapsingView(view: View) // 默认第一个
        fun changeCollapseState(isToCollapse: Boolean)
        fun autoAnimation()
    }

    interface CollapsingListener {
        fun beforeCollapseStateChange(isCollapsing: Boolean)
        fun onCollapseStateChange(isCollapsed: Boolean)
        fun collapseStateChanged(isCollapsed: Boolean)
    }
}




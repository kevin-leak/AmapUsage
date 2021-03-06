package com.example.amapusage.collapse

import android.view.View

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




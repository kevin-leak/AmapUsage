package com.example.amapusage.collapse

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.RelativeLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.example.amapusage.collapse.ControlSensorPerformer.Companion.AFTER_COLLLAPSING
import com.example.amapusage.collapse.ControlSensorPerformer.Companion.BEFORE_COLLLAPSING
import com.example.amapusage.collapse.ControlSensorPerformer.Companion.ON_COLLLAPSING


class ScrollCollapseLayout(context: Context?, attrs: AttributeSet?) :
    RelativeLayout(context, attrs), ControlSensorPerformer.Sensor {
    private val TAG = "ScrollCollapseLayout"
    private var collapseAnimation: ValueAnimator? = null
    private val collapseDelay: Long = 0
    private val collapseDuration: Long = 300L
    var isHeadCollapsing = false
        private set
    private var expandHeight: Float = -1f
    private var collapseHeight: Float = -1f
    private var lock = false
    private var listener: ControlSensorPerformer.CollapsingListener? = null
    // 不能作为反控制的view, 允许自己设定，默认为第一个子view
    private var collapseView: View? = null
    private val linkageMap = mutableMapOf<View, ControlSensorPerformer.Linkage?>()


    constructor(context: Context?) : this(context, null)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (expandHeight < 0) {
            collapseView = collapseView ?: getChildAt(0)
            expandHeight = collapseView?.measuredHeight?.toFloat() ?: 0f // 获取测量的最初值
            collapseHeight = collapseView?.minimumHeight?.toFloat() ?: 0f
        }
    }

    // 判断事件发生是否在当前view的位置
    private fun isTouchView(view: View, ev: MotionEvent): Boolean {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val x = location[0]
        val y = location[1]
        return ev.x > x && ev.x < x + view.width && ev.y > y && ev.y < y + view.height
    }

    override fun isCollapsed(): Boolean {
        return isHeadCollapsing
    }

    override fun animation() {
        if (lock) {
            Log.e(TAG, "animation lock state is $lock ")
            return
        }
        collapseAnimation = if (isHeadCollapsing) {
            ValueAnimator.ofFloat(collapseHeight, expandHeight) // 坍塌由低到高
        } else {
            ValueAnimator.ofFloat(expandHeight, collapseHeight) // 非坍塌有高到低
        }
        collapseAnimation?.interpolator = AccelerateInterpolator(4f)
        collapseAnimation?.duration = collapseDuration
        collapseAnimation?.startDelay = collapseDelay
        if (listener == null) listener = CollapsingListenerImpl()
        collapseAnimation?.doOnStart {
            lock = true
            listener?.beforeCollapsingStateChange(this)
        }
        collapseAnimation?.doOnEnd {
            lock = false
            isHeadCollapsing = !isHeadCollapsing
            listener?.collapsingStateChanged(this)
        }
        collapseAnimation?.addUpdateListener { animation ->
            val currentHeight = animation.animatedValue as Float
            collapseView?.layoutParams?.height = currentHeight.toInt()
            collapseView?.requestLayout()
            if (collapseAnimation?.isRunning == true) listener?.onCollapsingStateChange(this)
        }
        collapseAnimation?.start()
    }

    override fun bindCollapsingView(view: View): ControlSensorPerformer.Sensor {
        linkageMap[view] = null
        return this
    }

    override fun setLinkages(
        view: View?,
        linkage: ControlSensorPerformer.Linkage,
        tag: Int
    ): ControlSensorPerformer.Sensor {
        linkage.tag = tag
        if (view == null) {
            linkageMap.entries.forEach {
                linkageMap[it.key] = linkageMap[it.key] ?: linkage
            }
        } else {
            linkageMap[view] = linkage
        }
        return this
    }

    override fun getLinkages(): MutableMap<View, ControlSensorPerformer.Linkage?> {
        return linkageMap
    }


    fun setCollapsingListener(listener: ControlSensorPerformer.CollapsingListener) {
        this.listener = listener
    }

    override fun expand() {
        if (isHeadCollapsing) animation()
    }

    override fun collapsing() {
        if (!isHeadCollapsing) animation()
    }

    open class CollapsingListenerImpl(private val doAction: Boolean = false) :
        ControlSensorPerformer.CollapsingListener {
        private val TAG = "CollapsingListenerImpl"
        override fun beforeCollapsingStateChange(sensor: ControlSensorPerformer.Sensor) {
            Log.d(TAG, "beforeCollapsingStateChange: ${sensor.isCollapsed()}")
            if (doAction) actionImpl(BEFORE_COLLLAPSING, sensor)
        }

        override fun onCollapsingStateChange(sensor: ControlSensorPerformer.Sensor) {
            Log.d(TAG, "onCollapsingStateChange: ${sensor.isCollapsed()}")
            if (doAction) actionImpl(ON_COLLLAPSING, sensor)
        }

        override fun collapsingStateChanged(sensor: ControlSensorPerformer.Sensor) {
            Log.d(TAG, "collapsingStateChanged: ${sensor.isCollapsed()}")
            if (doAction) actionImpl(AFTER_COLLLAPSING, sensor)
        }

        private fun actionImpl(tag: Int, sensor: ControlSensorPerformer.Sensor) {
            sensor.getLinkages().entries.forEach {
                if (it.value?.tag == tag) it.value?.action(it.key)
            }
        }
    }

}
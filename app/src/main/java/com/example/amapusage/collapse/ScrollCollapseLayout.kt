package com.example.amapusage.collapse

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.RelativeLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnResume
import androidx.core.animation.doOnStart


class ScrollCollapseLayout(context: Context?, attrs: AttributeSet?) :
    RelativeLayout(context, attrs), ControlSensorPerformer.Sensor {
    private var touchSlop: Int = 0
    private lateinit var collapseAnimation: ValueAnimator
    private val collapseDelay: Long = 0
    private val collapseDuration: Long = 300L
    var isHeadCollapsing = false
        private set
    private var expandHeight: Float = -1f
    private var collapseHeight: Float = -1f
    private var lock = false
    private var listener: ControlSensorPerformer.CollapsingListener? = null
    private var collapseView: View? = null
    private var downY: Float = 0f

    constructor(context: Context?) : this(context, null)

    init {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        if (touchSlop == 0) touchSlop = 21
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (expandHeight < 0) {
            collapseView = collapseView ?: getChildAt(0)
            expandHeight = collapseView?.measuredHeight?.toFloat() ?: 0f // 获取测量的最初值
            collapseHeight = collapseView?.minimumHeight?.toFloat() ?: 0f
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (lock) return true
        return super.onInterceptTouchEvent(ev)
    }

    // 判断事件发生是否在当前view的位置
    private fun isTouchView(view: View?, ev: MotionEvent?): Boolean {
        if (view == null || ev == null) return false
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val x = location[0]
        val y = location[1]
        return ev.x > x && ev.x < x + view.width && ev.y > y && ev.y < y + view.height
    }


    override fun changeCollapseState(isToCollapse: Boolean) {
        return if (isToCollapse) collapsing() else expand()
    }

    override fun autoAnimation() {
        if (lock) return
        collapseAnimation = if (isHeadCollapsing) {
            ValueAnimator.ofFloat(collapseHeight, expandHeight) // 坍塌由低到高
        } else {
            ValueAnimator.ofFloat(expandHeight, collapseHeight) // 非坍塌有高到低
        }
        // fixme 后期改，这里动画不对，要和collapse相关联.
        collapseAnimation.apply {
            interpolator = AccelerateInterpolator(4f)
            duration = collapseDuration
            startDelay = collapseDelay
        }
        collapseAnimation.doOnStart {
            lock = true
            listener?.beforeCollapsingStateChange(this)
        }
        collapseAnimation.doOnEnd {
            lock = false
            isHeadCollapsing = !isHeadCollapsing
            listener?.collapsingStateChanged(this)
        }
        collapseAnimation.addUpdateListener { animation ->
            val currentHeight = animation.animatedValue as Float
            collapseView?.layoutParams?.height = currentHeight.toInt()
            collapseView?.requestLayout()
            if (collapseAnimation.isRunning) listener?.onCollapsingStateChange(this)
        }
        collapseAnimation.start()
    }

    fun setCollapsingListener(listener: ControlSensorPerformer.CollapsingListener) {
        this.listener = listener
    }

    private fun expand() {
        if (isHeadCollapsing && !lock) autoAnimation()
    }

    private fun collapsing() {
        if (!isHeadCollapsing && !lock) autoAnimation()
    }

    override fun isCollapsed(): Boolean {
        return isHeadCollapsing
    }

    override fun bindCollapsingView(view: View) {
        collapseView = view
    }

    open class CollapsingListenerImpl() :
        ControlSensorPerformer.CollapsingListener {
        override fun beforeCollapsingStateChange(sensor: ControlSensorPerformer.Sensor) {}
        override fun onCollapsingStateChange(sensor: ControlSensorPerformer.Sensor) {}
        override fun collapsingStateChanged(sensor: ControlSensorPerformer.Sensor) {}
    }

}
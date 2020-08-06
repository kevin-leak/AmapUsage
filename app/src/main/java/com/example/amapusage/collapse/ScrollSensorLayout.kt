package com.example.amapusage.collapse

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart


class ScrollSensorLayout(context: Context?, attrs: AttributeSet?) :
    RelativeLayout(context, attrs), IScrollSensor.Sensor {
    private var touchSlop: Int = 0
    private lateinit var collapseAnimation: ValueAnimator
    private val collapseDelay: Long = 0
    val collapseDuration: Long = 150L
    var isCollapsing = false
        private set
    private var expandHeight: Float = -1f
    private var collapseHeight: Float = -1f
    private var lock = false
    private var listener: IScrollSensor.CollapsingListener? = null
    private var collapseView: View? = null

    constructor(context: Context?) : this(context, null)

    init {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        if (touchSlop == 0) touchSlop = 21
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (expandHeight < 0) {
            collapseView = collapseView ?: getChildAt(0)
            expandHeight = collapseView?.measuredHeight?.toFloat() ?: 0f
            collapseHeight = collapseView?.minimumHeight?.toFloat() ?: 0f
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (lock) return true // 处理抖动问题
        return super.onInterceptTouchEvent(ev)
    }


    private fun isTouchView(view: View?, ev: MotionEvent?): Boolean {
        if (view == null || ev == null) return false
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val x = location[0]
        val y = location[1]
        return ev.x > x && ev.x < x + view.width && ev.y > y && ev.y < y + view.height
    }

    override fun autoAnimation() {
        if (lock) return
        collapseAnimation = if (isCollapsing) ValueAnimator.ofFloat(collapseHeight, expandHeight)
        else ValueAnimator.ofFloat(expandHeight, collapseHeight)
        collapseAnimation.apply {
            // fixme 差值还是需要改改
            interpolator = AccelerateDecelerateInterpolator()
            duration = collapseDuration
            startDelay = collapseDelay
        }
        collapseAnimation.doOnStart {
            lock = true
            listener?.beforeCollapseStateChange(isCollapsing)
        }
        collapseAnimation.doOnEnd {
            lock = false
            isCollapsing = !isCollapsing
            listener?.collapseStateChanged(isCollapsing)
        }
        collapseAnimation.addUpdateListener { animation ->
            val currentHeight = animation.animatedValue as Float
            collapseView?.layoutParams?.height = currentHeight.toInt()
            collapseView?.requestLayout()
            if (collapseAnimation.isRunning) listener?.onCollapseStateChange(isCollapsing)
        }
        collapseAnimation.start()
    }

    fun setCollapsingListener(listener: IScrollSensor.CollapsingListener) {
        this.listener = listener
    }

    override fun changeCollapseState(isToCollapse: Boolean): Unit =
        if (isToCollapse) collapsing() else expand()

    private fun expand(): Unit = if (isCollapsing && !lock) autoAnimation() else Unit
    private fun collapsing(): Unit = if (!isCollapsing && !lock) autoAnimation() else Unit
    override fun isCollapsed() = isCollapsing

    override fun bindCollapsingView(view: View) {
        collapseView = view
    }

    fun unLock() {
        lock = false
    }

    fun setLock() {
        lock = true
    }

    open class CollapsingListenerImpl() :
        IScrollSensor.CollapsingListener {
        override fun beforeCollapseStateChange(isCollapsing: Boolean) {}
        override fun onCollapseStateChange(isCollapsed: Boolean) {}
        override fun collapseStateChanged(isCollapsed: Boolean) {}
    }

}
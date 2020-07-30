package com.example.amapusage.collapse

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.ScrollView


class ScrollViewController : ScrollView, ControlSensorPerformer.Controller {

    private lateinit var sensor: ControlSensorPerformer.Sensor
    private var touchSlop: Int = 0
    private var downY: Float = 0f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int)
            : super(context, attr, defStyleAttr)

    init {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        if (touchSlop == 0) touchSlop = 21
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        var temp = parent
        while (temp !is ControlSensorPerformer.Sensor) temp = temp.parent
        sensor = temp
    }

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        // 防止被子view消费掉，先记录
        if (MotionEvent.ACTION_DOWN == e?.action) downY = e.y
        return super.onInterceptTouchEvent(e)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        // 子view没有产生消费，判断父view是否要消费
        if (ev?.action == MotionEvent.ACTION_MOVE) {
            // 非坍塌,手指向上滑动 -> 则坍塌
            if (!sensor.isCollapsed() && downY - ev.y > touchSlop) {
                sensor.changeCollapseState(true)
                return false
            }else if (sensor.isCollapsed() && ev.y - downY > touchSlop && scrollY == 0) {
                // 坍塌状态，手指向下滑动, 且处于顶端 -> 展开
                sensor.changeCollapseState(false)
                return false
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun fling(velocityY: Int) {
        super.fling((velocityY * 0.4).toInt())
    }
}
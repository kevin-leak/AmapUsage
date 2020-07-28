package com.example.amapusage.collapse

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
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
            : super(context, attr, defStyleAttr) {
        init()
    }

    private fun init() {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
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
            // 如果非坍塌且手指向上滑动，则坍塌
            if (!sensor.isCollapsed() && downY - ev.y > touchSlop) {
                sensor.collapsing()
                return false
            }
            if (sensor.isCollapsed() && ev.y - downY > touchSlop && scrollY == 0) {
                sensor.expand() // 坍塌状态，手指向下滑动且处于顶端
                return false
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun fling(velocityY: Int) {
        super.fling((velocityY * 0.3).toInt())
    }
}
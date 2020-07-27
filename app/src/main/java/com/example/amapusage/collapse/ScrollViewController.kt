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
        sensor.bindController(this)
    }


    // 父view设置了clickable则会收到UP事件，但是如果DOWN事件为true，同样也不收到UP
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> downY = ev.y
            MotionEvent.ACTION_UP -> {
                if (sensor.isCollapsing() && ev.y - downY > touchSlop && scrollY == 0) {
                    sensor.expand() // 坍塌状态，手指向下滑动且处于顶端
                    return true
                } else if (!sensor.isCollapsing() && downY - ev.y > touchSlop) {
                    scrollTo(0, 0)
                    sensor.collapsing() // 非坍塌状态，手指向上滑动
                    return true
                }
            }
        }
        return return super.onTouchEvent(ev)
    }

    override fun fling(velocityY: Int) {
        super.fling((velocityY * 0.5).toInt())
    }
}
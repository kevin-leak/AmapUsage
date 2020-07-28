package com.example.amapusage.collapse

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView

class RecycleViewController : RecyclerView, ControlSensorPerformer.Controller {

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
        if (touchSlop == 0) touchSlop = 21
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        var temp = parent
        while (temp !is ControlSensorPerformer.Sensor) temp = temp.parent
        sensor = temp
    }

    val TAG = "RecycleViewController"

    // 父view设置了clickable则会收到UP事件，但是如果DOWN事件为true，同样也不收到UP
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                downY = ev.y
                // 子view没有产生消费，判断父view是否要消费
                if (!sensor.isCollapsing()) sensor.collapsing()
            }
            MotionEvent.ACTION_MOVE -> {
                // 子view没有产生消费，判断父view是否要消费
                if (!sensor.isCollapsing()) {
                    sensor.collapsing()
                    smoothScrollToPosition(0)
                }
            }
            MotionEvent.ACTION_UP -> {
                // 子view没有产生消费，判断父view是否要消费
                if (sensor.isCollapsing() && ev.y - downY > touchSlop && !canScrollVertically(-1)) {
                    sensor.expand() // 坍塌状态，手指向下滑动且处于顶端
                } else if (!sensor.isCollapsing() && downY - ev.y > touchSlop) {
                    sensor.collapsing() // 非坍塌状态，手指向上滑动
                    smoothScrollToPosition(0)
                }
            }
        }
        return super.onTouchEvent(ev) // 这里对Down事件进行了消费.
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        return super.fling(velocityX, (velocityY * 0.3).toInt())
    }
}
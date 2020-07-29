package com.example.amapusage.collapse

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView

class RecycleViewController : RecyclerView, ControlSensorPerformer.Controller {
    val TAG = "RecycleViewController"
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

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        if (MotionEvent.ACTION_DOWN == e?.action) downY = e.y // 防止被消费掉，先记录
        return super.onInterceptTouchEvent(e)
    }

    // 父view设置了clickable则会收到UP事件，但是如果DOWN事件为true，同样也不收到UP
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_MOVE -> {
                // 非坍塌,手指向上滑动 -> 则坍塌
                if (!sensor.isCollapsed() && downY - ev.y > touchSlop) {
                    sensor.collapsing()
                    return false
                }
            }
            MotionEvent.ACTION_UP -> { // 必须在UP，不要在move否则，向上挪动一下容易引起震荡.
                // 坍塌状态，手指向下滑动, 且处于顶端 -> 展开
                if (sensor.isCollapsed() && ev.y - downY > touchSlop && !canScrollVertically(-1)) {
                    sensor.expand()
                    return false
                }
            }
        }
        return super.onTouchEvent(ev) // 这里对Down事件进行了消费.
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        return super.fling(velocityX, (velocityY * 0.25).toInt())
    }
}
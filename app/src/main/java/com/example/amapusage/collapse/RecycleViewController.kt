package com.example.amapusage.collapse

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView

class RecycleViewController(context: Context, attr: AttributeSet?, defStyleAttr: Int) :
    RecyclerView(context, attr, defStyleAttr), ControlSensorPerformer.Controller {

    private var touchSlop: Int = 0
    private var downY: Float = 0f

    constructor(context: Context) : this(context, null) {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        maxFlingVelocity = 4000
    }

    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0) {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    // 父view设置了clickable则会收到UP事件，但是如果DOWN事件为true，同样也不收到UP
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        var controller = parent
        while (controller !is ControlSensorPerformer.Sensor) controller = parent.parent
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> downY = ev.y
            MotionEvent.ACTION_UP -> {
                if (controller.isCollapsing() && ev.y - downY > touchSlop && !canScrollVertically(-1)) {
                    controller.animation() // 坍塌状态，手指向下滑动且处于顶端
                    return true // 如果没有Recycle会滑动，不好看
                } else if (!controller.isCollapsing() && downY - ev.y > touchSlop) {
                    stopScroll()
                    scrollToPosition(0)
                    controller.animation() // 非坍塌状态，手指向上滑动
                    return true // 如果没有Recycle会滑动，不好看
                }
            }
        }
        return super.onTouchEvent(ev) // 这里对Down事件进行了消费.
    }

    fun setMaxFlingVelocity(velocity: Int) {
        val field = this.javaClass.getDeclaredField("mMaxFlingVelocity");
        field.isAccessible = true;
        field.set(this, velocity);
    }
}
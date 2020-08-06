package com.example.amapusage.search

import android.content.Context
import android.opengl.Visibility
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.amapusage.R
import com.example.amapusage.model.LocationViewModel
import kotlinx.android.synthetic.main.item_location.view.*

/**
 * 默认展示和搜索展示的切换.向外通知数据的变化.
 * */
class EntityCheckAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    IEntityCheckSearch.IHintAdapter, View.OnClickListener {
    // search 变化, check 变化.

    private var footHolder: RecyclerView.ViewHolder? = null
    private var isShowFootItem: Boolean = false
    private lateinit var currentList: MutableLiveData<MutableList<CheckModel>>
    private lateinit var mContext: Context
    private lateinit var model: LocationViewModel
    var listener: IEntityCheckSearch.CheckListener? = null

    private var footView: View? = null
    private val VIEW_TYPE_FOOT = 1

    constructor(mContext: Context, model: LocationViewModel) : this() {
        this.mContext = mContext
        this.model = model
        currentList = model.currentModelList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType != VIEW_TYPE_FOOT) {
            val view = LayoutInflater.from(mContext).inflate(R.layout.item_location, null)
            DataViewHolder(view)
        } else {
            if (footHolder != null) {
                footHolder!!
            } else {
                footView =
                    LayoutInflater.from(mContext)
                        .inflate(R.layout.item_location_foot, parent, false)
                footHolder = object : RecyclerView.ViewHolder(footView!!) {}
                footHolder!!
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        //当position是最后一个的时候，也就是比list的数量多一个的时候，则表示FooterView
        return if (position + 1 == currentList.value!!.size) {
            VIEW_TYPE_FOOT
        } else super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return currentList.value!!.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == itemCount - 1) return
        holder.itemView.locationChecker.tag = position // 标记
        holder.itemView.locationChecker.setOnClickListener(this)
        holder.itemView.tvLocationName.text = currentList.value!![position].sendModel.placeTitle
        holder.itemView.tvLocationDesc.text = currentList.value!![position].distanceDetails
        holder.itemView.locationChecker.isChecked = currentList.value!![position].isChecked
        if (currentList.value!![position].isChecked) { // 复用的时候也会被调用.
            checkPosition = position
        }
    }

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { flashyEditClick(itemView.locationChecker) }
        }
        private fun flashyEditClick(view: View) { // 模拟一个searchContentEdit的点击事件
            view.dispatchTouchEvent(
                MotionEvent.obtain(
                    SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN,
                    view.left + 5f,
                    view.top + 5f,
                    0
                )
            )
            view.dispatchTouchEvent(
                MotionEvent.obtain(
                    SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_UP,
                    view.left + 5f,
                    view.top + 5f,
                    0
                )
            )
        }
    }

    override fun switchData(data: MutableLiveData<MutableList<CheckModel>>) {
        currentList = data
        checkPosition = -1
        notifyDataSetChanged()
    }

    override fun removeFootItem() {
        if (footView == null) return
        isShowFootItem = false
        notifyItemRemoved(itemCount)
    }

    override fun addFootItem() {
        if (footView != null) return
        isShowFootItem = true
        notifyItemInserted(itemCount)
    }

    var checkPosition = -1
    override fun onClick(buttonView: View) {
        if (currentList.value == null || checkPosition > currentList.value!!.size) return
        if (checkPosition != -1) {
            currentList.value!![checkPosition].isChecked = false
            notifyItemChanged(checkPosition)
        }
        currentList.value!![buttonView.tag as Int].isChecked = true
        model.checkModel.value = currentList.value!![buttonView.tag as Int]
        notifyItemChanged(buttonView.tag as Int)
        // 不能放在绑定的位置，复用的时候回调用，放在这里同时可以保证
        // 默认为第一个的时候，不发生移动
        listener?.hasBeChecked((buttonView.tag as Int))
    }

    fun notifyItemWithOutFoot() {
        val endCount = if (footHolder != null) itemCount - 1 else itemCount
        if (endCount >= 0) {
            notifyItemRangeChanged(0, endCount)
        }
    }

}


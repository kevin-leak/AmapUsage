package com.example.amapusage.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.amapusage.R
import com.example.amapusage.model.LocationModel
import com.example.amapusage.model.LocationViewModel
import kotlinx.android.synthetic.main.item_location.view.*

class EntityCheckAdapter() : RecyclerView.Adapter<EntityCheckAdapter.DataViewHolder>(),
    IEntityCheckSearch.IHintAdapter, View.OnClickListener {
    private lateinit var currentList: MutableList<LocationModel>
    var isSearch = false // 只做数据交换
        set(value) {
            if (value) {
                currentCheckPosition = checkPosition // 保存
                currentList = model.searchModelList.value!!
            } else {
                model.searchModelList.value?.clear() // 清空数据
                currentList = model.currentModelList.value!!
            }
            checkPosition = if (value) -1 else currentCheckPosition
            field = value
        }

    private lateinit var mContext: Context
    private lateinit var model: LocationViewModel

    constructor(mContext: Context, model: LocationViewModel) : this() {
        this.mContext = mContext
        this.model = model
        // 默认是current以及checkPosition
        currentList = model.currentModelList.value!!
        currentCheckPosition = 0
        checkPosition = currentCheckPosition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_location, null)
        return DataViewHolder(view)
    }

    override fun getItemCount(): Int = currentList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.itemView.locationChecker.tag = position // 标记
        holder.itemView.locationChecker.setOnClickListener(this)
        holder.itemView.tvLocationName.text = currentList[position].placeTitle
        holder.itemView.tvLocationDesc.text = currentList[position].details
        holder.itemView.locationChecker.isChecked = currentList[position].isChecked
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


    var checkPosition: Int = -1 // item 和 position相互绑定
        private set(value) {
            if (field != -1 && currentList.size > 0) {
                currentList[field].isChecked = false
                notifyItemChanged(field)
            }
            if (value == -1) {
                model.sendModel.value = null
            } else if (currentList.size > 0) {
                model.sendModel.value = currentList[value]
                currentList[value].isChecked = true
                notifyItemChanged(value)
            }
            field = value
        }
    private var currentCheckPosition: Int = 0 // tmp，默认为0

    override fun onClick(buttonView: View) { // 只改变position
        checkPosition = buttonView.tag as Int
    }
}

package com.example.amapusage.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.amapusage.R
import com.example.amapusage.factory.GetLocationOperator
import com.example.amapusage.model.LocationViewModel
import kotlinx.android.synthetic.main.item_location.view.*

/**
 * 默认展示和搜索展示的切换.向外通知数据的变化.
 * */
class EntityCheckAdapter() : RecyclerView.Adapter<EntityCheckAdapter.DataViewHolder>(),
    IEntityCheckSearch.IHintAdapter, View.OnClickListener {
    // search 变化, check 变化.

    private lateinit var currentList: MutableList<CheckModel>
    var isSearch = false // 只做数据交换
        set(value) {
            if (value) {
                currentCheckPosition = checkPosition // 保存
                currentList = model.searchModelList.value!!
            } else {
                model.searchModelList.value?.clear() // 清空数据
                currentList = model.currentModelList.value!!
                if (currentCheckPosition <= 0) {
                    field = value
                    return
                }
                GetLocationOperator.moveToSelect(currentList[currentCheckPosition].model.latLonPoint)
            }
            checkPosition = if (value) -1 else currentCheckPosition
            field = value
        }

    private lateinit var mContext: Context
    private lateinit var model: LocationViewModel
    lateinit var listener: IEntityCheckSearch.CheckListener

    constructor(mContext: Context, model: LocationViewModel) : this() {
        this.mContext = mContext
        this.model = model
        currentList = ArrayList()
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
        holder.itemView.tvLocationName.text = currentList[position].model.placeTitle
        holder.itemView.tvLocationDesc.text = currentList[position].model.details
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
            if (field != -1 && currentList.size > 0 && field < currentList.size) {
                currentList[field].isChecked = false
                notifyItemChanged(field)
            }
            if (value == -1) {
                model.checkModel.value = null
            } else if (currentList.size > 0) {
                model.checkModel.value = currentList[value]
                currentList[value].isChecked = true
                notifyItemChanged(value)
            }
            field = value
        }
    private var currentCheckPosition: Int = 0 // tmp，默认为0

    override fun onClick(buttonView: View) { // 只改变position
        checkPosition = buttonView.tag as Int
        listener.checkByClick(currentList[checkPosition])
    }

    override fun clearAddEntity(it: MutableList<CheckModel>) { // 这是清空再加
        currentList.clear()
        currentList.addAll(it)
        if (it.size == 0) {
            currentCheckPosition = -1
            checkPosition = -1
            return
        }
        notifyDataSetChanged()
        if (it.size > 0 && !isSearch) {
            currentCheckPosition = 0
            checkPosition = currentCheckPosition
        } else {
            checkPosition = -1
        }
    }

    override fun insertEntity(it: MutableList<CheckModel>, index: Int) {
        currentList.addAll(it)
        notifyDataSetChanged()
    }

    override fun addMoreEntity(it: MutableList<CheckModel>) {
        currentList.addAll(it)
        notifyDataSetChanged()
    }

    fun addRefreshItem() {

    }

    open class CheckListenerImpl : IEntityCheckSearch.CheckListener {
        override fun checkByClick(model: CheckModel) {}
        override fun checkByAuto(model: CheckModel) {}
    }
}

package com.example.amapusage.search

import android.content.Context
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.toSpannable
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

    private var footHolder: RecyclerView.ViewHolder? = null
    private var isShowFootItem: Boolean = false
    private lateinit var currentList: MutableLiveData<MutableList<CheckModel>>
    private lateinit var mContext: Context
    private lateinit var model: LocationViewModel
    var listener: IEntityCheckSearch.CheckListener? = null
    private var footView: View? = null

    companion object {
        const val VIEW_TYPE_FOOT = 1
    }

    constructor(mContext: Context, model: LocationViewModel) : this() {
        this.mContext = mContext
        this.model = model
        currentList = model.normalList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType != VIEW_TYPE_FOOT) {
            DataViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_location, null))
        } else {
            if (footHolder != null) {
                footView?.visibility = View.GONE
                footHolder!!
            } else {
                footView = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_location_foot, parent, false)
                footView?.visibility = View.GONE
                object : RecyclerView.ViewHolder(footView!!) {}
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position + 1 == currentList.value!!.size) VIEW_TYPE_FOOT
        else super.getItemViewType(position)
    }

    override fun getItemCount(): Int = currentList.value!!.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == itemCount - 1) return
        holder.itemView.locationChecker.tag = position // 标记
        holder.itemView.locationChecker.setOnClickListener(this)
        holder.itemView.tvLocationName.text = currentList.value!![position].sendModel.placeTitle
        holder.itemView.tvLocationDesc.text = currentList.value!![position].distanceDetails
        holder.itemView.locationChecker.isChecked = currentList.value!![position].isChecked
    }

    override fun switchData(data: MutableLiveData<MutableList<CheckModel>>) {
        currentList = data
        checkPosition = -1
        notifyDataSetChanged()
    }

    override fun removeFootItem() {
        isShowFootItem = false
        notifyItemRemoved(itemCount)
        footView?.visibility = View.GONE
    }

    override fun addFootItem() {
        isShowFootItem = true
        notifyItemInserted(itemCount)
        footView?.visibility = View.VISIBLE
    }

    override fun onClick(buttonView: View) {
        if (currentList.value == null || checkPosition > currentList.value!!.size) return
        unCheck()
        checkModel(buttonView.tag as Int)
        listener?.hasBeChecked((buttonView.tag as Int))
    }

    var checkPosition = -1
    private fun unCheck() {
        if (checkPosition == -1 && !currentList.value!![0].isSearch) checkPosition = 0
        if (checkPosition != -1) {
            currentList.value!![checkPosition].isChecked = false
            notifyItemChanged(checkPosition)
        }
    }

    private fun checkModel(position: Int) {
        currentList.value!![position].isChecked = true
        model.checkModel.value = currentList.value!![position]
        checkPosition = position
        notifyItemChanged(position)
    }

    fun notifyItemWithOutFoot() {
        val endCount = if (footHolder != null) itemCount - 1 else itemCount
        if (endCount >= 0) {
            notifyItemRangeChanged(0, endCount)
        }
    }

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { itemView.locationChecker.performClick() }
        }
    }
}


package com.example.amapusage.search

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.amapusage.R
import com.example.amapusage.model.CheckModel
import com.example.amapusage.model.LocationViewModel
import kotlinx.android.synthetic.main.item_location.view.*
import javax.security.auth.login.LoginException
import kotlin.math.log

/**
 * 默认展示和搜索展示的切换.向外通知数据的变化.
 * */
class EntityCheckAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    IEntityCheckSearch.IHintAdapter, View.OnClickListener {

    private var footHolder: RecyclerView.ViewHolder? = null
    private var isShowFootItem: Boolean = false
    private lateinit var data: MutableLiveData<MutableList<CheckModel>>
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
        data = model.normalList
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
        return if (isDataArea(position)) VIEW_TYPE_FOOT
        else super.getItemViewType(position)
    }

    override fun getItemCount(): Int = data.value!!.size + 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isDataArea(position)) return
        holder.itemView.locationChecker.tag = position // 标记
        holder.itemView.locationChecker.setOnClickListener(this)
        holder.itemView.tvLocationName.text = data.value!![position].sendModel.placeTitle
        holder.itemView.tvLocationDesc.text = data.value!![position].distanceDetails
        holder.itemView.locationChecker.isChecked = data.value!![position].isChecked
    }

    private fun isDataArea(position: Int) = position == itemCount - 1

    override fun switchData(data: MutableLiveData<MutableList<CheckModel>>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun removeFootItem() {
        isShowFootItem = false
        notifyItemRemoved(itemCount)
        footView?.visibility = View.GONE
    }

    override fun addFootItem() {
        if (itemCount <= 1) return
        isShowFootItem = true
        notifyItemInserted(itemCount)
        footView?.visibility = View.VISIBLE
    }

    override fun onClick(buttonView: View) = exchangeCheckStatus(buttonView.tag as Int)

    private fun exchangeCheckStatus(position: Int) {
        unCheck()
        checkModel(position)
        listener?.hasBeChecked(position)
        Log.e("kyle-map", "exchangeCheckStatus: ")
    }

    private fun unCheck() {
        val checkModel = model.checkModel.value
        if (checkModel != null && data.value?.indexOf(checkModel) != -1) {
            val index = data.value?.indexOf(checkModel) ?: 0
            data.value!![index].isChecked = false
            notifyItemChanged(index)
        }
    }

    private fun checkModel(position: Int) {
        data.value!![position].isChecked = true
        model.checkModel.value = data.value!![position]
        notifyItemChanged(position)
    }

    fun notifyItemWithOutFoot() {
        val endCount = if (footHolder != null) itemCount - 1 else itemCount
        if (endCount >= 0) {
            notifyItemRangeChanged(0, endCount)
        }
    }

    fun checkCurrent(): Boolean {
        if (data.value.isNullOrEmpty()) return false
        if (data.value!![0].isPointEqual(model.myLocation)) {
            exchangeCheckStatus(0)
            return true
        }
        return false
    }

    var snapshot = -1
    fun takeASnapshot() {
        snapshot = data.value?.indexOf(model.checkModel.value) ?: 0
        snapshot = if (snapshot == -1) 0 else snapshot
    }

    fun restoreSnapshot(): Int {
        if (data.value!!.size <= snapshot) return -1
        exchangeCheckStatus(snapshot)
        return snapshot
    }

    fun getPosition(): Int {
        if (data.value == null || model.checkModel.value == null) return -1
        return data.value!!.indexOf(model.checkModel.value!!)
    }

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { itemView.locationChecker.performClick() }
        }
    }
}


package com.example.amapusage.search

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.amapusage.R
import kotlinx.android.synthetic.main.item_location.view.*
import java.util.*

class EntityCheckAdapter(private var mContext: Context?, private var mList: ArrayList<String>?) :
    RecyclerView.Adapter<EntityCheckAdapter.DataViewHolder>(),
    CompoundButton.OnCheckedChangeListener, IEntityCheckSearch.IHintAdapter {

    // 两份数据，在搜索之前的一份数据，是地图大头针指向的位置为距离搜
    // 第二份是，搜索的数据，这两份数据进行切换.
    // 当发送check的时候，显示在地图上，并取出.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_location, null)
        return DataViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList!!.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.itemView.locationChecker.tag = position // 标记
        currentHolder = holder
        holder.itemView.tvLocationName.text = mList!![position]
        holder.itemView.tvLocationDesc.text = mList!![position] + "this is description"
        holder.itemView.locationChecker.setOnCheckedChangeListener(this)
    }

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                itemView.locationChecker.isChecked = !itemView.locationChecker.isChecked
            }
        }
    }

    private var currentHolder: DataViewHolder? = null
    private var currentCheckPosition: Int = -1
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (currentCheckPosition != -1 && currentHolder?.layoutPosition != currentCheckPosition) {
            currentHolder?.itemView?.locationChecker?.isChecked = !isChecked
        }
    }

}

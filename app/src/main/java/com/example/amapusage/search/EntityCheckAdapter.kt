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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_location, null)
        return DataViewHolder(view)
    }

    override fun getItemCount(): Int = mList!!.size

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

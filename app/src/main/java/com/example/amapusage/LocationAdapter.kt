package com.example.amapusage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_location.view.*
import java.util.*

class LocationAdapter(private var mContext: Context?, private var mList: ArrayList<String>?) :
    RecyclerView.Adapter<LocationAdapter.DataViewHolder>(),CompoundButton.OnCheckedChangeListener {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_location, null)
        return DataViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList!!.size
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.itemView.locationChecker.tag = position // 标记
        currentHolder = holder
        holder.itemView.tvLocationName.text = mList!![position]
        holder.itemView.locationChecker.setOnCheckedChangeListener(this)
    }

    //创建ViewHolder
    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        init {
            itemView.setOnClickListener {
                itemView.locationChecker.isChecked = !itemView.locationChecker.isChecked }
        }
    }

    private var currentHolder: DataViewHolder? = null
    var currentCheckPosition :Int = -1
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (currentCheckPosition != -1 && currentHolder?.layoutPosition != currentCheckPosition){
            currentHolder?.itemView?.locationChecker?.isChecked = !isChecked
        }
    }

}

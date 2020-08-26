package com.example.amapusage.sheet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.amapusage.R

class BottomSheetAdapter() : RecyclerView.Adapter<BottomSheetAdapter.SheetHolder>() {

    companion object {
        const val SHEET_TITLE = 0
        const val SHEET_NORMAL = SHEET_TITLE + 1
        const val SHEET_FOCUS = SHEET_NORMAL + 1
    }

    private var focusAction: SheetAction? = null
    private var actions: MutableList<out SheetAction> = mutableListOf()
    var listener: SheetClickListener? = null
    var focusHolder: SheetHolder? = null

    constructor(actions: MutableList<out SheetAction>) : this() {
        this.actions = actions
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SheetHolder {
        return if (SHEET_FOCUS == viewType) {
            focusHolder!!
        } else {
            val resId = when (viewType) {
                SHEET_TITLE -> R.layout.item_bottom_sheet_title
                else -> R.layout.item_normal_bottom_sheet
            }
            val itemView = LayoutInflater.from(parent.context).inflate(resId, null)
            SheetHolder(itemView)
        }
    }

    fun setAction(actions: MutableList<out SheetAction>) {
        this.actions = actions
        notifyDataSetChanged()
    }

    fun setFocusAction(action: SheetAction) {
        focusAction = action
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: SheetHolder, position: Int) {
        holder.itemView.setOnClickListener {
            listener?.onColumnClickListener(position, actions[position])
        }
        holder.binData(actions[position])
    }

    override fun getItemCount(): Int = actions.size

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> SHEET_TITLE
            itemCount - 1 -> if (focusHolder != null) SHEET_FOCUS else SHEET_NORMAL
            else -> SHEET_NORMAL
        }
    }

    fun setColumnClick(listener: SheetClickListener) {
        this.listener = listener
    }

    open class SheetHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open fun binData(sheetAction: SheetAction) {
            itemView.findViewById<TextView>(R.id.item_bottom_sheet_desc)?.text =
                sheetAction.description
        }
    }

}
package com.example.amapusage.sheet.maps

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.amapusage.R
import com.example.amapusage.sheet.BottomSheetAdapter
import com.example.amapusage.sheet.SheetAction

class MapFocusHolder(itemView: View) : BottomSheetAdapter.SheetHolder(itemView) {

    override fun binData(sheetAction: SheetAction) {
        super.binData(sheetAction)
        val action = sheetAction as TaxiSheetAction
        itemView.findViewById<TextView>(R.id.title).text = action.title
        itemView.findViewById<ImageView>(R.id.actionIcon)
    }

}
package com.example.amapusage.sheet

interface SheetClickListener {
    fun onColumnClickListener(position: Int, sheetAction: SheetAction)
}
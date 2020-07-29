package com.example.amapusage.search

import android.widget.EditText

interface IHintSearchView {
    fun outEditMode()
    fun enterEditMode()
    fun setSearchListener(listener: OnSearchChangeListener)
    fun getEditView(): EditText

    interface OnSearchChangeListener {
        fun onEnterModeChange(isEnter: Boolean)
        fun sourceCome(data: String)
        fun sourceChanging(data: String)
        fun beforeSourceChange(toString: String)
    }

    interface IHintAdapter
}
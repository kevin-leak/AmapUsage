package com.example.amapusage.search

import android.os.IBinder
import android.text.Editable
import android.widget.EditText

interface IEntityCheckSearch {
    interface OnSearchChangeListener {
        fun onEnterModeChange(isEnter: Boolean)
        fun sourceCome(data: String)
        fun sourceChanging(data: String)
        fun beforeSourceChange(toString: String)
    }

    interface IHintAdapter

    fun outEditMode()
    fun enterEditMode()
    fun setSearchListener(listener: OnSearchChangeListener)
    fun setText(text: String)
    fun getText(): Editable?
}
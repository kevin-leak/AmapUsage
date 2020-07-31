package com.example.amapusage.search

import android.text.Editable

interface IEntityCheckSearch {
    interface OnSearchListener {
        fun onEnterModeChange(isEnter: Boolean)
        fun sourceCome(data: String) // 按下确认键
        fun sourceChanging(data: String)
        fun beforeSourceChange(toString: String)
    }

    interface IHintAdapter

    fun outEditMode()
    fun enterEditMode()
    fun setSearchListener(lt: OnSearchListener)
    fun setText(text: String)
    fun getText(): Editable?
}
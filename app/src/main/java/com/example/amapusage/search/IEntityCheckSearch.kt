package com.example.amapusage.search

import android.text.Editable
import com.example.amapusage.model.LocationModel

interface IEntityCheckSearch {
    interface OnSearchListener {
        fun onEnterModeChange(isEnter: Boolean)
        fun sourceCome(data: String) // 按下确认键
        fun sourceChanging(data: String)
        fun beforeSourceChange(toString: String)
    }

    interface IHintAdapter {
        fun addEntity(it: MutableList<LocationModel>)
        fun insertEntity(it:MutableList<LocationModel>)
    }

    fun exitEditMode()
    fun enterEditMode()
    fun setSearchListener(lt: OnSearchListener)
    fun setText(text: String)
    fun getText(): Editable?
}
package com.example.amapusage.search

import androidx.lifecycle.MutableLiveData
import com.example.amapusage.model.CheckModel

interface IEntityCheckSearch {
    interface OnSearchListener {
        fun onEnterModeChange(isEnter: Boolean) // 重新获取焦点也会调用
        fun sourceCome(data: String) // 按下确认键
        fun sourceChanging(data: String)
        fun beforeSourceChange(toString: String)
        fun onSearchModeChange(isSearch: Boolean)
        fun beforeSearchModeChange(isSearch: Boolean)
    }

    interface IHintAdapter {
        fun switchData(data: MutableLiveData<MutableList<CheckModel>>)
        fun removeFootItem()
        fun addFootItem()
    }

    interface CheckListener {
        fun hasBeChecked(position: Int)
    }

    interface textTimeEndListener{
        fun textCome(text:String)
    }

    interface KeyboardListener {
        fun keyboardClose()
        fun keyboardOpen()
    }

    fun exitEditMode()
    fun enterEditMode()
    fun addSearchListener(lt: OnSearchListener)
    fun exitSearchMode()
}
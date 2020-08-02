package com.example.amapusage.search

import android.text.Editable

interface IEntityCheckSearch {
    interface OnSearchListener {
        fun onEnterModeChange(isEnter: Boolean) // 重新获取焦点也会调用
        fun sourceCome(data: String) // 按下确认键
        fun sourceChanging(data: String)
        fun beforeSourceChange(toString: String)
        fun onSearchModeChange(isSearch: Boolean)
    }

    interface IHintAdapter {
        fun clearAddEntity(it: MutableList<CheckModel>)
        fun insertEntity(it:MutableList<CheckModel>, index:Int)
        fun addMoreEntity(it: MutableList<CheckModel>)
    }

    interface CheckListener {
        fun checkByClick(model :CheckModel)
        fun checkByAuto(model :CheckModel)
    }

    fun exitEditMode()
    fun enterEditMode()
    fun setSearchListener(lt: OnSearchListener)
    fun setText(text: String)
    fun getText(): Editable?
}
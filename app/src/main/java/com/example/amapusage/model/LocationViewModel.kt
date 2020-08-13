package com.example.amapusage.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class LocationViewModel : ViewModel() {
    // 构建三种需要通知的数据，分别是：
    // 1. 移动地图直接产生的locationModelList
    // 2. 搜索产生的字符串
    // 3. checked 的item
    var normalList = MutableLiveData<MutableList<CheckModel>>()
    var searchList = MutableLiveData<MutableList<CheckModel>>() // search的时候的list
    var checkModel = MutableLiveData<CheckModel?>() // 最终要修改的数据.
    var currentModel = MutableLiveData<CheckModel>() // 因为可能频繁触发，保存当前定位model
    var snapshot = -1 // 如果数据再处于加载中，这样可以先保存index但是无法变化checkModel


    fun resetSearch() {
        searchList.value = mutableListOf()
        clearCheckModel()
    }

    fun takeASnapshot() {
        snapshot = normalList.value?.indexOf(checkModel.value) ?: 0
        snapshot = if (snapshot == -1) 0 else snapshot
    }

    fun reSetAllData() {
        resetSearch()
        normalList.value = mutableListOf()
    }

    fun setDefaultCheck() {
        if (normalList.value!!.size > 0) { // 默认选择第一个
            normalList.value!![0].isChecked = true
            checkModel.value = normalList.value!![0]
        }
    }

    init {
        reSetAllData()
    }

    fun restoreSnapshot() {
        if (normalList.value!!.size <= snapshot) return
        checkModel.value = normalList.value!![snapshot]
    }

    private fun clearCheckModel() = run { checkModel.value = null }
    fun isChecked() = checkModel.value != null
}
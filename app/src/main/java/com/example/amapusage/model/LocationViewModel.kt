package com.example.amapusage.model

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.log

open class LocationViewModel : ViewModel() {
    // 构建三种需要通知的数据，分别是：
    // 1. 移动地图直接产生的locationModelList
    // 2. 搜索产生的字符串
    // 3. checked 的item
    var normalList = MutableLiveData<MutableList<CheckModel>>()
    var searchList = MutableLiveData<MutableList<CheckModel>>() // search的时候的list
    var checkModel = MutableLiveData<CheckModel?>() // 最终要修改的数据.
    var currentModel = MutableLiveData<CheckModel>() // 因为可能频繁触发，保存当前定位model， TODO
    private var tempInt = -1 // 如果数据再处于加载中，这样可以先保存index但是无法变化checkModel


    fun resetSearch() {
        searchList.value = mutableListOf()
        clearCheckModel()
    }

    fun takeASnapshot() {
        tempInt = normalList.value?.indexOf(checkModel.value) ?: 0
        tempInt = if (tempInt == -1) 0 else tempInt
    }

    fun reSetAllData() {
        resetSearch()
        normalList.value = mutableListOf()
    }

    fun setDefaultCheck() {
        if (normalList.value!!.size > 0) { // 默认选择第一个
            normalList.value!![0].isChecked = true
            checkModel.value = normalList.value!![0]
            currentModel.value = checkModel.value
        }
    }

    init {
        reSetAllData()
    }

    fun restoreSnapshot() = run { checkModel.value = normalList.value!![tempInt] }
    private fun clearCheckModel() = run { checkModel.value = null }
    fun isChecked() = checkModel.value != null
}
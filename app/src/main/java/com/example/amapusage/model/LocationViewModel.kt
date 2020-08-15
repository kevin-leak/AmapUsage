package com.example.amapusage.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amap.api.location.AMapLocation
import com.amap.api.services.core.LatLonPoint

open class LocationViewModel() : ViewModel() {
    // 构建三种需要通知的数据，分别是：
    // 1. 移动地图直接产生的locationModelList
    // 2. 搜索产生的字符串
    // 3. checked 的item
    var normalList = MutableLiveData<MutableList<CheckModel>>()
    var searchList = MutableLiveData<MutableList<CheckModel>>() // search的时候的list
    var checkModel = MutableLiveData<CheckModel?>() // 最终要修改的数据.
    var myLocation: AMapLocation? = null
    var snapshot = -1 // 如果数据再处于加载中，这样可以先保存index但是无法变化checkModel


    init {
        normalList.value = mutableListOf()
        searchList.value = mutableListOf()
        checkModel.value = null
    }

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

    fun setDefaultCheck(point: LatLonPoint) {
        if (normalList.value.isNullOrEmpty()) return
        if (!normalList.value!![0].isPointEqual(point)) {
            val checkModel = CheckModel(point)
            checkModel.sendModel.placeTitle = "Location"
            checkModel.distanceDetails = "100m内"
            normalList.value!!.add(0, checkModel)
        }
        normalList.value!![0].isChecked = true
        checkModel.value = normalList.value!![0]
    }

    fun restoreSnapshot() {
        if (normalList.value!!.size <= snapshot) return
        checkModel.value = normalList.value!![snapshot]
    }

    private fun clearCheckModel() = run { checkModel.value = null }
    fun isChecked() = checkModel.value != null
}
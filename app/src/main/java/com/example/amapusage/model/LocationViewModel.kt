package com.example.amapusage.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amap.api.location.AMapLocation
import com.amap.api.services.core.LatLonPoint

open class LocationViewModel() : ViewModel() {
    var normalList = MutableLiveData<MutableList<CheckModel>>()
    var searchList = MutableLiveData<MutableList<CheckModel>>() // search的时候的list
    var checkModel = MutableLiveData<CheckModel?>() // 最终要修改的数据.
    var myLocation: AMapLocation? = null

    init {
        normalList.value = mutableListOf()
        searchList.value = mutableListOf()
        checkModel.value = null
    }

    fun resetSearch() {
        searchList.value = mutableListOf()
        clearCheckModel()
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


    private fun clearCheckModel() = run { checkModel.value = null }
    fun isChecked() = checkModel.value != null
    fun resetNormal() {
        normalList.value = mutableListOf()
        clearCheckModel()
    }
}
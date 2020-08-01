package com.example.amapusage.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.amapusage.model.LocationModel

open class LocationViewModel : ViewModel() {
    // 构建三种需要通知的数据，分别是：
    // 1. 移动地图直接产生的locationModelList
    // 2. 搜索产生的字符串
    // 3. checked 的item
    var queryText = MutableLiveData<String>()
    var currentModelList = MutableLiveData<MutableList<LocationModel>>() // 非search的list
    var searchModelList = MutableLiveData<MutableList<LocationModel>>() // search的时候的list
    var sendModel = MutableLiveData<LocationModel?>() // 最终要修改的数据.

    init {
        currentModelList.value = ArrayList()
        searchModelList.value = ArrayList()
    }

    fun getQueryText(): LiveData<String> = queryText

    open fun setQueryText(string: String) {
        this.queryText.value = string
    }

}
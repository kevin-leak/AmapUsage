package com.example.amapusage.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.amapusage.search.CheckModel

open class LocationViewModel : ViewModel() {
    // 构建三种需要通知的数据，分别是：
    // 1. 移动地图直接产生的locationModelList
    // 2. 搜索产生的字符串
    // 3. checked 的item
    var currentModelList = MutableLiveData<MutableList<CheckModel>>()
    var searchModelList = MutableLiveData<MutableList<CheckModel>>() // search的时候的list
    var checkModel = MutableLiveData<CheckModel?>() // 最终要修改的数据.

    init {
        currentModelList.value = mutableListOf()
        searchModelList.value = mutableListOf()
    }

}
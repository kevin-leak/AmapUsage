package com.example.amapusage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationViewModel : ViewModel() {
    // 构建三种需要通知的数据，分别是：
    // 1. 移动地图直接产生的locationModelList
    // 2. 搜索产生的字符串
    // 3. checked 的item
    var queryText = MutableLiveData<String>()
    var locationModelList = MutableLiveData<LocationModel>()
    var sendLocationModel = MutableLiveData<LocationModel>()
}
package com.example.amapusage.model

import androidx.lifecycle.LiveData

class LocationModel : LiveData<LocationModel>() {
    // 建立与map产生的数据的关系.做好解析和翻译.
    var isChecked: Boolean = false
}
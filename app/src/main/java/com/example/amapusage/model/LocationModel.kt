package com.example.amapusage.model

import androidx.lifecycle.LiveData

class LocationModel(var isChecked: Boolean = false, var placeTitle: String, var details: String) :
    LiveData<LocationModel>() {
    // 建立与map产生的数据的关系.做好解析和翻译.
    override fun toString(): String {
        return "LocationModel(isChecked=$isChecked, placeTitle='$placeTitle', details='$details')"
    }
}
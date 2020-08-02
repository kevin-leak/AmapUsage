package com.example.amapusage.model

import androidx.lifecycle.LiveData
import com.amap.api.services.core.LatLonPoint

class LocationModel :
    LiveData<LocationModel>() {


    lateinit var placeTitle: String
    lateinit var details: String
    var isChecked: Boolean = false
    lateinit var latLonPoint: LatLonPoint

    // 建立与map产生的数据的关系.做好解析和翻译.
    override fun toString(): String {
        return "LocationModel(isChecked=$isChecked, placeTitle='$placeTitle', details='$details')"
    }

}
package com.example.amapusage.model

import androidx.lifecycle.LiveData
import com.amap.api.services.core.LatLonPoint

open class LocationModel(var latLonPoint: LatLonPoint) :
    LiveData<LocationModel>() {

    lateinit var placeTitle: String
    lateinit var details: String
    // 建立与map产生的数据的关系.做好解析和翻译.
    override fun toString(): String {
        return "LocationModel( placeTitle='$placeTitle', details='$details')"
    }
}
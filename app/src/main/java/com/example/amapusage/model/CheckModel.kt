package com.example.amapusage.model

import com.amap.api.services.core.LatLonPoint

class CheckModel(var lonPoint: LatLonPoint, var isChecked: Boolean = false) {

    lateinit var distanceDetails: String
    var isSearch = false
    var sendModel: LocationModel = LocationModel().apply {
        latitude = lonPoint.latitude
        longitude = lonPoint.longitude
    }

}
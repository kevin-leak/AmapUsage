package com.example.amapusage.search

import com.amap.api.services.core.LatLonPoint
import com.example.amapusage.model.LocationModel

class CheckModel(var lonPoint: LatLonPoint, var isChecked: Boolean = false) {

    lateinit var distanceDetails: String

    var sendModel: LocationModel = LocationModel().apply {
        latitude = lonPoint.latitude
        longitude = lonPoint.longitude
    }

    override fun toString(): String {
        return "CheckModel(isChecked=$isChecked)"
    }
}
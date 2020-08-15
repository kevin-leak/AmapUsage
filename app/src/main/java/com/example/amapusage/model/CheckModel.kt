package com.example.amapusage.model

import com.amap.api.location.AMapLocation
import com.amap.api.services.core.LatLonPoint
import kotlin.math.abs

class CheckModel(var lonPoint: LatLonPoint, var isChecked: Boolean = false) {

    var distanceDetails: String = ""
    var isSearch = false
    var sendModel: LocationModel = LocationModel().apply {
        latitude = lonPoint.latitude
        longitude = lonPoint.longitude
    }

    fun isPointEqual(p0: LatLonPoint?): Boolean {
        if (p0 == null) return false
        return abs(p0.latitude - lonPoint.latitude) < 0.00002f
                && abs(p0.longitude - lonPoint.longitude) < 0.00002f
    }

    fun isPointEqual(p0: AMapLocation?): Boolean {
        if (p0 == null) return false
        return isPointEqual(LatLonPoint(p0.latitude, p0.longitude))
    }

}
package com.example.amapusage.model

import com.amap.api.services.core.LatLonPoint

class CheckModel(var lonPoint: LatLonPoint, var isChecked: Boolean = false) {

    lateinit var distanceDetails: String
    var isSearch = false

    var sendModel: LocationModel = LocationModel().apply {
        latitude = lonPoint.latitude
        longitude = lonPoint.longitude
    }

    override fun toString(): String {
        return "CheckModel(isChecked=$isChecked)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CheckModel

        if (lonPoint != other.lonPoint) return false
        if (isChecked != other.isChecked) return false
        if (distanceDetails != other.distanceDetails) return false
        if (isSearch != other.isSearch) return false
        if (sendModel != other.sendModel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lonPoint.hashCode()
        result = 31 * result + isChecked.hashCode()
        result = 31 * result + distanceDetails.hashCode()
        result = 31 * result + isSearch.hashCode()
        result = 31 * result + sendModel.hashCode()
        return result
    }


}
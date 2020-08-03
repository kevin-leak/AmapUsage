package com.example.amapusage.search

import com.amap.api.services.core.LatLonPoint
import com.example.amapusage.model.LocationModel

class CheckModel(var sendModel: LocationModel, var isChecked: Boolean = false) {
    override fun toString(): String {
        return "CheckModel(isChecked=$isChecked)"
    }
}
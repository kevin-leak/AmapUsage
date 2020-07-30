package com.example.amapusage.factory

import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.model.Marker

interface IMapOperator {
    interface Operator {
        fun prepareForWork(aMap: AMap, listener: LocationSourceLister):Operator    // 必须实现.
        fun buildMapBaseConfig(): AMap      // 配置
        fun bindCurrentButton(button:ImageButton):Operator
        fun bindMapPin(mapPin: ImageView):Operator
        fun clearMapPin()
        fun setUpMapPin()
        fun getMap(): AMap
        fun moveToCurrent()                 // 定位
        fun endOperate()
        fun queryEntry(queryText: String)
    }

    interface LocationSourceLister {
        fun moveCameraFinish()
        fun onMoveChange()
    }

//    interface locationDataSource
}
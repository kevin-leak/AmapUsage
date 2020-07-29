package com.example.amapusage.factory

import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.model.Marker

interface IMapOperator {
    interface Operator {
        fun prepareForWork(aMap: AMap, lister: LocationSourceLister)    // 必须实现.
        fun buildMapBaseConfig(): AMap      // 配置
        fun buildCenterMark(): Marker       // 中心位置
        fun clearMark(aMap: AMap)
        fun getMap(): AMap
        fun moveToCurrent()                 // 定位
        fun endOperate()
        fun syncMapCamera(queryText: String)
    }

    interface LocationSourceLister {
        fun locationSync(dataSource: AMapLocation)
    }

//    interface locationDataSource
}
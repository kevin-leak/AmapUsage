package com.example.amapusage.factory

import android.widget.ImageButton
import android.widget.ImageView
import com.amap.api.maps.AMap
import com.amap.api.maps.TextureMapView
import com.example.amapusage.search.CheckModel

interface IMapOperator {
    interface Operator {
        fun preWork(tMV: TextureMapView, lt: LocationSourceLister): Operator  // 不持有tMV
        fun buildMapBaseConfig(): AMap      // 配置
        fun bindCurrentButton(btn: ImageButton): Operator
        fun bindMapPin(pin: ImageView): Operator
        fun clearMapPin(): Operator
        fun setUpMapPin(): Operator
        fun getMap(): AMap
        fun moveToCurrent()                 // 定位
        fun endOperate()
        fun queryByText(queryText: String)
    }

    interface LocationSourceLister {
        fun moveCameraFinish()
        fun onMoveChange()
        fun startLoadData()
        fun loadDataDone()
    }

//    interface locationDataSource
}
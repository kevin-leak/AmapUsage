package com.example.amapusage

import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.MyLocationStyle

object AMapConfiguration {
    fun buildUIConfig(map: AMap) {
        map.myLocationStyle = MyLocationStyle().apply {// 蓝点设置
            interval(2000) //连续定位模式下的刷新间隔
            strokeColor(Color.argb(0, 0, 0, 0))
            radiusFillColor(Color.argb(0, 0, 0, 0))
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE) // 随着定位而移动
            showMyLocation(true)// 蓝点是否显示，5.1.0版本后支持
        }
        map.apply {
            moveCamera(CameraUpdateFactory.zoomTo(16f)) // 当前位置缩放
            uiSettings.isMyLocationButtonEnabled = false // 是否显示定位锚点图标
            isMyLocationEnabled = true                  // 是否显示蓝点
            uiSettings.isZoomControlsEnabled = false    // 是否显示缩放按钮
            uiSettings.logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_RIGHT // 设置地图logo显示在右下方
            uiSettings.setLogoBottomMargin(-50)         //隐藏logo
        }
    }
}
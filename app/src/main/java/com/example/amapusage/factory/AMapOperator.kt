package com.example.amapusage.factory

import android.graphics.Color
import android.graphics.Point
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.*
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.TranslateAnimation
import com.example.amapusage.App
import com.example.amapusage.R
import com.example.amapusage.utils.ScreenUtils
import kotlin.math.sqrt

object AMapOperator : AMap.OnCameraChangeListener, IMapOperator.Operator {
    private val mLocationClient: AMapLocationClient = AMapLocationClient(App.getAppContext())
    private var centerMarker: Marker? = null
    private lateinit var aMap: AMap
    private var currentLocation: AMapLocation? = null
    private lateinit var listener: IMapOperator.LocationSourceLister

    override fun prepareForWork(aMap: AMap, listener: IMapOperator.LocationSourceLister) {
        AMapOperator.aMap = aMap
        this.listener = listener
        mLocationClient.setLocationListener { aMapLocation -> currentLocation = aMapLocation }
        mLocationClient.startLocation() //启动定位我当前位置
        buildMapBaseConfig()
        aMap.setOnCameraChangeListener(this)
        buildCenterMark()
    }

    override fun buildMapBaseConfig(): AMap {
        aMap.myLocationStyle = MyLocationStyle().apply {// 蓝点设置
            interval(2000) //连续定位模式下的刷新间隔
            strokeColor(Color.parseColor("#2A117DD3"))
            radiusFillColor(Color.parseColor("#2A117DD3"))
            myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER) // 持续定位不移动中心点，这个很重要.
            showMyLocation(true)// 蓝点是否显示，5.1.0版本后支持
        }
        aMap.apply {
            moveCamera(CameraUpdateFactory.zoomTo(16f)) // 当前位置缩放
            uiSettings.isMyLocationButtonEnabled = false // 是否显示定位锚点图标
            isMyLocationEnabled = true                  // 是否显示蓝点
            uiSettings.isZoomControlsEnabled = false    // 是否显示缩放按钮
            uiSettings.logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_RIGHT // 设置地图logo显示在右下方
            uiSettings.setLogoBottomMargin(-50)         //隐藏logo
            uiSettings.isScaleControlsEnabled = true    // 比例尺
        }
        return aMap
    }

    override fun buildCenterMark(): Marker {
        val latLng: LatLng = aMap.cameraPosition.target
        val screenPosition: Point = aMap.projection.toScreenLocation(latLng)
        if (centerMarker != null) return centerMarker!!
        centerMarker = aMap.addMarker(
            MarkerOptions().anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_pin))
        )
        centerMarker!!.setPositionByPixels(screenPosition.x, screenPosition.y) // 设置不移动
        centerMarker!!.zIndex = 1f
        return centerMarker!!
    }

    private fun buildMarkAnimation(): Marker {
        if (centerMarker == null) buildCenterMark()
        //根据屏幕距离计算需要移动的目标点
        val latLng = centerMarker!!.position
        val point: Point = aMap.projection.toScreenLocation(latLng)
        point.y -= ScreenUtils.dip2px(App.getAppContext(), 125f)
        val target: LatLng = aMap.projection.fromScreenLocation(point)
        val animation: Animation = TranslateAnimation(target)
        animation.setInterpolator { input -> // 模拟重加速度的interpolator
            if (input <= 0.5) {
                (0.5f - 2 * (0.5 - input) * (0.5 - input)).toFloat()
            } else {
                (0.5f - sqrt((input - 0.5f) * (1.5f - input).toDouble())).toFloat()
            }
        }
        animation.setDuration(600)
        centerMarker?.setAnimation(animation)
        centerMarker?.startAnimation()
        return centerMarker!!
    }

    override fun clearMark(aMap: AMap) {
        aMap.clear()
        centerMarker = null
    }

    override fun getMap(): AMap {
        return aMap
    }

    override fun moveToCurrent() {
        if (currentLocation == null) return
        val latLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        getMap().moveCamera(CameraUpdateFactory.changeLatLng(latLng))
        // 如果来自于moveToCurrent于cheacked那么不需要collapse，但是如果来自于touch
        // 如果处于搜索状态，moveCurrent则需要移动
    }

    override fun endOperate() {
        mLocationClient.stopLocation()
    }

    override fun onCameraChangeFinish(cameraPosition: CameraPosition?) {
        buildMarkAnimation().startAnimation()
        listener.moveCameraFinish()
    }

    override fun onCameraChange(cameraPosition: CameraPosition?) {
        listener.onMoveChange()
    }

    override fun queryEntry(queryText: String) {

    }

}
package com.example.amapusage.factory

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.view.isVisible
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.Animation
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.example.amapusage.R
import com.example.amapusage.utils.ScreenUtils
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 设置基础的UI且可扩展，内置client，query空实现.
 * */
open class AMapOperator : AMap.OnCameraChangeListener, IMapOperator.Operator,
    PoiSearch.OnPoiSearchListener {

    private lateinit var animationPin: TranslateAnimation
    private lateinit var clientOption: AMapLocationClientOption
    private var mapPin: ImageView? = null
    private lateinit var currentButton: ImageButton
    private lateinit var mLocationClient: AMapLocationClient
    internal lateinit var aMap: AMap
    internal var myLocation: AMapLocation? = null
    lateinit var listener: IMapOperator.LocationSourceLister
    private val deta = 0.00002f // 这和两个location的取值有关系，有的四舍五入了.
    lateinit var context: Context

    private var centerMarker: Marker? = null

    override fun preWork(tMV: TextureMapView, lt: IMapOperator.LocationSourceLister): AMapOperator {
        context = tMV.context
        aMap = tMV.map
        this.listener = lt
        buildMapBaseConfig()
        setUpClient()
        mLocationClient.startLocation() //启动定位我当前位置
        aMap.setOnCameraChangeListener(this)
        addMarkerInScreenCenter()
        return this
    }

    private fun addMarkerInScreenCenter() {
        val option = MarkerOptions()
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin))
        centerMarker = aMap.addMarker(option)
        aMap.addMarker(option)
        resetCenterMark()
        startJumpAnimation()
    }

    fun resetCenterMark() {
        centerMarker?.isVisible = true
        val latLng = aMap.cameraPosition.target
        val screenPosition = aMap.projection.toScreenLocation(latLng)
        //设置Marker在屏幕上,不跟随地图移动
        centerMarker?.setPositionByPixels(screenPosition.x, screenPosition.y)
    }

    open fun startJumpAnimation() {
        //根据屏幕距离计算需要移动的目标点
        val latLng: LatLng = centerMarker!!.position
        val point = aMap.projection.toScreenLocation(latLng)
        point.y -= ScreenUtils.dip2px(context, 30f)
        val target = aMap.projection
            .fromScreenLocation(point)
        //使用TranslateAnimation,填写一个需要移动的目标点
        val animation: Animation =
            com.amap.api.maps.model.animation.TranslateAnimation(target)
        animation.setInterpolator { input -> // 模拟重加速度的interpolator
            if (input <= 0.5) {
                (0.5f - 2 * (0.5 - input) * (0.5 - input)).toFloat()
            } else {
                (0.5f - sqrt((input - 0.5f) * (1.5f - input).toDouble())).toFloat()
            }
        }
        animation?.setDuration(600)
        //设置动画
        centerMarker?.setAnimation(animation)
        //开始动画
        centerMarker?.startAnimation()
    }

    private fun setUpClient() {
        mLocationClient = AMapLocationClient(context)
        clientOption = AMapLocationClientOption().apply {
            isOnceLocation = true
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        }
        mLocationClient.setLocationOption(clientOption)
        mLocationClient.setLocationListener { aMapLocation ->
            myLocation = aMapLocation
            moveToCurrent()
            mLocationClient.stopLocation()
        }
    }

    override fun buildMapBaseConfig(): AMap {
        aMap.myLocationStyle = MyLocationStyle().apply {// 蓝点设置
            interval(2000) //连续定位模式下的刷新间隔,如果不设置圆圈就会一直放大
            myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_base))
            strokeColor(Color.parseColor("#2A117DD3"))
            radiusFillColor(Color.parseColor("#2A117DD3"))
            strokeWidth(2f)
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

    override fun bindCurrentButton(btn: ImageButton): AMapOperator = apply { currentButton = btn }
    override fun clearMapPin(): AMapOperator = apply { centerMarker?.isVisible = false }
    override fun setUpMapPin(): AMapOperator = apply { centerMarker?.isVisible = true }
    override fun endOperate() = mLocationClient.stopLocation()
    override fun getMap(): AMap = aMap


    override fun moveToCurrent() {
        if (myLocation == null) return
        mLocationClient.startLocation()
        val latLng = LatLng(myLocation!!.latitude, myLocation!!.longitude)
        getMap().animateCamera(CameraUpdateFactory.changeLatLng(latLng), 600,
            object : AMap.CancelableCallback {
                override fun onFinish() {}
                override fun onCancel() {}
            })
    }

    override fun onCameraChangeFinish(cameraPosition: CameraPosition) {
        if (abs(cameraPosition.target.latitude - myLocation!!.latitude) < deta
            && abs(cameraPosition.target.longitude - myLocation!!.longitude) < deta
        ) {
            currentButton.apply { setImageDrawable(resources.getDrawable(R.drawable.ic_gps_blue)) }
        } else {
            currentButton.apply { setImageDrawable(resources.getDrawable(R.drawable.ic_gps_gray)) }
        }
        if (mapPin?.isVisible == true) mapPin?.startAnimation(animationPin)
        listener.moveCameraFinish()
        startJumpAnimation()
    }

    override fun onCameraChange(cameraPosition: CameraPosition?) {
        listener.onMoveChange()
        resetCenterMark()
    }

    override fun queryByText(queryText: String) {}


    override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {}

    override fun onPoiSearched(poiResult: PoiResult?, resultCode: Int) {}

}
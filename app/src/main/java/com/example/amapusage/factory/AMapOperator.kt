package com.example.amapusage.factory

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.TranslateAnimation
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.example.amapusage.R
import com.example.amapusage.utils.ScreenUtils
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 设置基础的UI且可扩展，内置client，query空实现.
 * */
open class AMapOperator : AMap.OnCameraChangeListener, IMapOperator.Operator,
    PoiSearch.OnPoiSearchListener {

    var isNeedCenterPin: Boolean = true
        set(value) {
            if (!value) this.clearCenterMark()
            field = value
        }
    private var isFirst: AtomicBoolean = AtomicBoolean().apply { set(true) }
    private var positionMark: Marker? = null
    private lateinit var clientOption: AMapLocationClientOption
    private lateinit var currentButton: ImageButton
    private lateinit var mLocationClient: AMapLocationClient
    internal lateinit var aMap: AMap
    internal var myLocation: AMapLocation? = null
    lateinit var listener: IMapOperator.LocationSourceLister
    private val deta = 0.00002f // 这和两个location的取值有关系，有的四舍五入了.
    lateinit var context: Context
    private var centerMarker: Marker? = null
    private var markBaseMap = mutableMapOf<String, Marker>()

    override fun preWork(tMV: TextureMapView, lt: IMapOperator.LocationSourceLister): AMapOperator {
        context = tMV.context
        aMap = tMV.map
        this.listener = lt
        buildMapBaseConfig()
        setUpClient()
        aMap.setOnCameraChangeListener(this)
        return this
    }

    fun restartClient(){
        mLocationClient.startLocation()
        isFirst.set(true)
    }

    private fun addMarkerInScreenCenter() {
        if (isNeedCenterPin){
            val option = MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin))
            centerMarker = aMap.addMarker(option)
            aMap.addMarker(option)
            resetCenterMark()
        }
    }

    fun resetCenterMark() {
        centerMarker?.isVisible = true
        val latLng = aMap.cameraPosition.target
        val screenPosition = aMap.projection.toScreenLocation(latLng)
        centerMarker?.setPositionByPixels(screenPosition.x, screenPosition.y)
    }

    open fun startJumpAnimation() {
        //根据屏幕距离计算需要移动的目标点
        val latLng: LatLng = centerMarker!!.position
        val point = aMap.projection.toScreenLocation(latLng)
        point.y -= ScreenUtils.dip2px(context, 30f)
        val target = aMap.projection.fromScreenLocation(point)
        val animationPin: Animation = TranslateAnimation(target)
        animationPin.setInterpolator { input -> // 模拟重加速度的interpolator
            if (input <= 0.5) {
                (0.5f - 2 * (0.5 - input) * (0.5 - input)).toFloat()
            } else {
                (0.5f - sqrt((input - 0.5f) * (1.5f - input).toDouble())).toFloat()
            }
        }
        animationPin.setDuration(600)
        centerMarker?.setAnimation(animationPin)
        centerMarker?.startAnimation()
    }

    private fun setUpClient() {
        mLocationClient = AMapLocationClient(context)
        clientOption = AMapLocationClientOption().apply {
//            isOnceLocation = true
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        }
        mLocationClient.setLocationOption(clientOption)
        mLocationClient.setLocationListener { aMapLocation ->
            locationCome(aMapLocation)
        }
        mLocationClient.startLocation() //启动定位我当前位置
    }

    open fun locationCome(aMapLocation: AMapLocation?) {
        myLocation = aMapLocation
        if (isFirst.get()) initAction()
    }

    override fun bindCurrentButton(btn: ImageButton, state: IMapOperator.LocateCurrentState): AMapOperator {
        currentButton = btn
        currentButton.tag = false
        currentButton.setOnClickListener {
            if (state.performLocateCurrent(currentButton.tag as Boolean)
                || currentButton.tag as Boolean) return@setOnClickListener
            currentButton.tag = true
        }
        return this
    }

    override fun initAction() {
        moveToCurrent()
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
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16f)) // 当前位置缩放
        aMap.isMyLocationEnabled = true // 是否显示蓝点
        aMap.uiSettings.apply {
            isMyLocationButtonEnabled = false // 是否显示定位锚点图标
            isZoomControlsEnabled = false    // 是否显示缩放按钮
            logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_RIGHT // 设置地图logo显示在右下方
            setLogoBottomMargin(-50)         //隐藏logo
            isScaleControlsEnabled = true    // 比例尺
        }
        return aMap
    }

    fun addPositionMarker(latLng: LatLng) {
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin))//大头针图标
        aMap.addMarker(markerOptions)
    }

    fun removeAddPositionMarker(latLng: LatLng) { // base和mark 不共生，且base会遗留.
        if (positionMark == null){ // 第一次发生check
            val positionOptions = MarkerOptions()
            positionOptions.position(latLng)
            positionOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin))//大头针图标
            positionMark = aMap.addMarker(positionOptions)
        }else{ // 如果发生了check, mark有存在，则重置mark的位置，并移除当前位置的base，且构建出以前位置的base
            val l = LatLng(positionMark!!.position.latitude, positionMark!!.position.longitude)
            addPositionMarkerBase(l)
            val key = "" + latLng.latitude + "#" + latLng.longitude
            if (markBaseMap.containsKey(key)) {
                markBaseMap[key]!!.remove()
                markBaseMap.remove(key)
            }
            positionMark!!.position = latLng
        }
    }

    private fun addPositionMarkerBase(latLng: LatLng) {
        val key = "" + latLng.latitude + "#" + latLng.longitude
        if (markBaseMap.containsKey(key)) return
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_base_green))//大头针图标
        val mark = aMap.addMarker(markerOptions);
        markBaseMap[key] = mark
    }

    fun clearAllMarkerBase() {
        markBaseMap.clear()
    }

    override fun moveToCurrent() {
        if (myLocation == null) return
        mLocationClient.startLocation()
        val latLng = LatLng(myLocation!!.latitude, myLocation!!.longitude)
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16f))
        getMap().animateCamera(CameraUpdateFactory.changeLatLng(latLng), 600, null)
    }

    override fun onCameraChangeFinish(cameraPosition: CameraPosition) {
        changeCurrentButtonState(cameraPosition)
        listener.moveCameraFinish()
        if (centerMarker ==  null) addMarkerInScreenCenter()
        else startJumpAnimation()
        if (isFirst.compareAndSet(true, false)) initActionDone()
    }

    private fun changeCurrentButtonState(cameraPosition: CameraPosition) {
        var backID = R.drawable.ic_gps_gray
        if (abs(cameraPosition.target.latitude - myLocation!!.latitude) < deta
            && abs(cameraPosition.target.longitude - myLocation!!.longitude) < deta
        ) {
            backID = R.drawable.ic_gps_blue
        }
        currentButton.setImageDrawable(ContextCompat.getDrawable(context, backID))
        currentButton.tag = false
    }

    override fun setUpCenterMark(): AMapOperator = apply {
        if (centerMarker == null){
            addMarkerInScreenCenter()
        }else{
            centerMarker?.isVisible = true
            resetCenterMark()
        }
    }

    open fun initActionDone() {}
    override fun getMap(): AMap = aMap
    override fun queryByText(queryText: String) {}
    override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {}
    override fun endOperate() = mLocationClient.stopLocation()
    fun clearPositionMark() = apply { positionMark?.remove() }
    override fun onPoiSearched(poiResult: PoiResult?, resultCode: Int) {}
    override fun clearCenterMark(): AMapOperator = apply { centerMarker?.isVisible = false }
    override fun onCameraChange(cameraPosition: CameraPosition?) = listener.onMoveChange()

}
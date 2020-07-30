package com.example.amapusage.factory

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.ImageView
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.animation.Animation
import com.example.amapusage.App
import com.example.amapusage.R
import kotlin.math.abs
import kotlin.math.sqrt


object AMapOperator : AMap.OnCameraChangeListener, IMapOperator.Operator {
    private lateinit var clientOption: AMapLocationClientOption
    private lateinit var mapPin: ImageView
    private lateinit var currentButton: ImageButton
    private val mLocationClient: AMapLocationClient = AMapLocationClient(App.getAppContext())
    private lateinit var aMap: AMap
    private lateinit var currentLocation: AMapLocation
    private lateinit var listener: IMapOperator.LocationSourceLister
    private const val deta = 0.00002f // 这和两个location的取值有关系，有的四舍五入了.

    override fun prepareForWork(aMap: AMap, listener: IMapOperator.LocationSourceLister): AMapOperator {
        AMapOperator.aMap = aMap
        this.listener = listener
        clientOption = AMapLocationClientOption().apply {
            isOnceLocation = true
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        }
        mLocationClient.setLocationOption(clientOption)
        mLocationClient.setLocationListener { aMapLocation ->
            currentLocation = aMapLocation
            moveToCurrent()
            mLocationClient.stopLocation()
        }
        buildMapBaseConfig()
        mLocationClient.startLocation() //启动定位我当前位置
        aMap.setOnCameraChangeListener(this)
        return this
    }

    override fun buildMapBaseConfig(): AMap {
        aMap.myLocationStyle = MyLocationStyle().apply {// 蓝点设置
            interval(2000) //连续定位模式下的刷新间隔,如果不设置圆圈就会一直放大
            myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_gps_base))
            strokeColor(Color.parseColor("#2A117DD3"))
            radiusFillColor(Color.parseColor("#2A117DD3"))
            strokeWidth(2f)
            myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER) // 持续定位不移动中心点，这个很重要.
            showMyLocation(true)// 蓝点是否显示，5.1.0版本后支持
        }
        aMap.apply {
            moveCamera(CameraUpdateFactory.zoomTo(18f)) // 当前位置缩放
            uiSettings.isMyLocationButtonEnabled = false // 是否显示定位锚点图标
            isMyLocationEnabled = true                  // 是否显示蓝点
            uiSettings.isZoomControlsEnabled = false    // 是否显示缩放按钮
            uiSettings.logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_RIGHT // 设置地图logo显示在右下方
            uiSettings.setLogoBottomMargin(-50)         //隐藏logo
            uiSettings.isScaleControlsEnabled = true    // 比例尺
        }
        return aMap
    }

    override fun bindCurrentButton(button: ImageButton): AMapOperator {
        this.currentButton = button
        return this
    }

    override fun bindMapPin(mapPin: ImageView): AMapOperator {
        this.mapPin = mapPin
        return this
    }

    override fun clearMapPin() {
        mapPin.visibility = View.GONE
    }

    override fun setUpMapPin() {
        mapPin.visibility = View.VISIBLE
    }

    override fun getMap(): AMap {
        return aMap
    }

    override fun moveToCurrent() {
        mLocationClient.startLocation()
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        getMap().animateCamera(CameraUpdateFactory.changeLatLng(latLng), 600,
            object : AMap.CancelableCallback {
                override fun onFinish() {}
                override fun onCancel() {}
            })
    }

    override fun endOperate() {
        mLocationClient.stopLocation()
    }

    override fun onCameraChangeFinish(cameraPosition: CameraPosition?) {
        val latLng = cameraPosition?.target
        if (abs(latLng!!.latitude - currentLocation.latitude) < deta && abs(latLng.longitude - currentLocation.longitude) < deta) {
            currentButton.setImageDrawable(currentButton.context.getDrawable(R.drawable.ic_gps_blue))
        } else {
            currentButton.setImageDrawable(currentButton.context.getDrawable(R.drawable.ic_gps_gray))
        }
        mapPin.startAnimation(TranslateAnimation(0f, 0f, 0f, -60f).apply {
            duration = 400
            repeatMode = Animation.REVERSE
            repeatCount = 3
            interpolator = LinearInterpolator()
        })

        //使用TranslateAnimation,填写一个需要移动的目标点
        mapPin.startAnimation((TranslateAnimation(0f, 0f, 0f, -100f).apply {
            interpolator = Interpolator { input -> // 模拟重加速度的interpolator
                if (input <= 0.5) {
                    (0.5f - 2 * (0.5 - input) * (0.5 - input)).toFloat()
                } else {
                    (0.5f - sqrt((input - 0.5f) * (1.5f - input).toDouble())).toFloat()
                }
            }
            duration = 600
        }))
        listener.moveCameraFinish()
    }

    override fun onCameraChange(cameraPosition: CameraPosition?) {
        listener.onMoveChange()
    }

    override fun queryEntry(queryText: String) {

    }

}
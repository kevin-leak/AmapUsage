package com.example.amapusage

import android.util.Log
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationListener
import kotlin.math.log

object IMapClient {
    //声明AMapLocationClient类对象
    private val mLocationClient: AMapLocationClient = AMapLocationClient(App.getAppContext())
    val TAG = "kyle"
    fun getLocation(info:InfoArrivals) {
        mLocationClient.setLocationListener { amapLocation ->
            if (amapLocation != null) {
                if (amapLocation.errorCode == 0) {
                    info.arrival(amapLocation)
                }
                Log.e(TAG, "getLocation:  $amapLocation.toString()" )
            }
        }
        //启动定位
        mLocationClient.startLocation()
    }

    interface InfoArrivals{
        fun arrival(location: AMapLocation)
    }

    fun destroyClient(){
        mLocationClient.stopLocation()
    }
}
package com.example.amapusage.factory

import android.util.Log
import com.amap.api.maps.model.CameraPosition
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.example.amapusage.model.LocationModel

object GetLocationOperator : AMapOperator() {


    // 只要camera 发生移动则一定是被选中的location

    // adapter两个状态，

    override fun onCameraChangeFinish(cameraPosition: CameraPosition) {
        super.onCameraChangeFinish(cameraPosition)
        // 加载数据
        // 拿到位置
        val target = cameraPosition.target
    }

    // 区别对待：第一种，移动地图的数据
    //

    private var searchScope = 100
    private lateinit var centerQuery: PoiSearch.Query
    private var centerLocationPage = 0

    override fun initData() {
        currentLocation?.let {
            mapCenterLocationSource()
        }
    }

    override fun queryEntry(queryText: String) {
        mapCenterLocationSource()
    }

    private fun mapCenterLocationSource() {
        if (currentLocation == null) return
        centerQuery = PoiSearch.Query("", "", "")
        centerQuery.pageSize = 20 // 设置每页最多返回多少条poiitem
        centerQuery.pageNum = centerLocationPage //设置查询页码
        val poiSearch = PoiSearch(context, centerQuery)
        poiSearch.setOnPoiSearchListener(this)
        val latLonPoint = LatLonPoint(currentLocation!!.latitude, currentLocation!!.longitude)
        poiSearch.bound = PoiSearch.SearchBound(latLonPoint, searchScope)
        poiSearch.searchPOIAsyn()
    }


    fun loadMoreLocationInfo() {

    }


    override fun onPoiSearched(poiResult: PoiResult?, rCode: Int) {

        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (poiResult?.query != null) {
                if (poiResult.query == centerQuery) {
                    dealCenterQuery(poiResult)
                }
            } else {
                Log.e(TAG, "onPoiSearched: ")
            }
        }
    }

    val TAG = "GetLocationOperator"
    private fun dealCenterQuery(result: PoiResult) {
        val poiItems: List<PoiItem> = result.pois // 取得第一页的poiitem数据，页数从数字0开始
        val data: MutableList<LocationModel> = ArrayList()
        if (poiItems.isNotEmpty()) {
            for (poiItem in poiItems) {
                val model = LocationModel().apply {
                    placeTitle = poiItem.title
                    details = buildDetailsLocation(poiItem.snippet)
                    latLonPoint = poiItem.latLonPoint
                }
                data.add(model)
            }
        }
        listener.sourceCome(data)
    }

    private fun buildDetailsLocation(s: String): String {
        return searchScope.toString() + "m | $s"
    }
}
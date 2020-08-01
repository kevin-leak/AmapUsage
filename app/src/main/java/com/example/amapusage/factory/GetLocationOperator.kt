package com.example.amapusage.factory

import com.amap.api.maps.model.CameraPosition
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiSearch

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

    private var centerLocationPage = 0

    override fun queryEntry(queryText: String) {

    }

    fun mapCenterLocationSource() {
        val centerQuery = PoiSearch.Query("", "", "")
        centerQuery.pageSize = 10 // 设置每页最多返回多少条poiitem
        centerQuery.pageNum = centerLocationPage //设置查询页码
        val poiSearch = PoiSearch(context, centerQuery)
        poiSearch.setOnPoiSearchListener(this)
        poiSearch.bound = PoiSearch.SearchBound(
            LatLonPoint(currentLocation.latitude, currentLocation.longitude),
            1000
        )
        poiSearch.searchPOIAsyn()
    }

    fun loadMoreLocationInfo() {

    }

    override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {

    }
}
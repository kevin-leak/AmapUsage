package com.example.amapusage.factory

import android.graphics.Bitmap
import android.util.Log
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.example.amapusage.model.LocationModel
import com.example.amapusage.search.CheckModel

object GetLocationOperator : AMapOperator() {

    // 两种move，touch move, select move(currentButton， checked)
    private var lock = false
    var isNeedQuery = true
    private var searchScope = 100
    private lateinit var currentCenterQuery: PoiSearch.Query
    private lateinit var currentCenterPoint: LatLonPoint
    private var loadMoreQuery: PoiSearch.Query? = null

    override fun onCameraChangeFinish(cameraPosition: CameraPosition) {
        super.onCameraChangeFinish(cameraPosition)
        if (isNeedQuery) { // 自动搜索的，移动到屏幕中心.
            val target = cameraPosition.target
            queryByMove(LatLonPoint(target.latitude, target.longitude))
        } else if (!isNeedQuery) isNeedQuery = !isNeedQuery
    }

    override fun queryEntry(queryText: String) {
        if (lock) return
        lock = true
        currentCenterQuery = PoiSearch.Query(queryText, "", currentLocation?.cityCode)
        currentCenterQuery.pageSize = 20
        currentCenterQuery.pageNum = 0
        val poiSearch = PoiSearch(context, currentCenterQuery)
        poiSearch.setOnPoiSearchListener(this)
        poiSearch.searchPOIAsyn()
    }

    // 三种肯能，第一次moveCurrent，touchMove，currentButton
    private fun queryByMove(latLonPoint: LatLonPoint) {
        if (lock) return
        lock = true
        currentCenterPoint = latLonPoint
        currentCenterQuery = PoiSearch.Query("", "", "")
        searchScope = 100
        currentCenterQuery.pageSize = 20
        currentCenterQuery.pageNum = 0
        val poiSearch = PoiSearch(context, currentCenterQuery)
        poiSearch.setOnPoiSearchListener(this)
        poiSearch.bound = PoiSearch.SearchBound(latLonPoint, searchScope)
        poiSearch.searchPOIAsyn()
    }

    fun loadMore(latLonPoint: LatLonPoint) {
        if (lock) return
        loadMoreQuery = PoiSearch.Query("", "", "")
        loadMoreQuery!!.pageNum = loadMoreQuery!!.pageNum + 1
        val poiSearch = PoiSearch(context, loadMoreQuery)
        poiSearch.setOnPoiSearchListener(this)
        poiSearch.bound = PoiSearch.SearchBound(latLonPoint, searchScope + searchScope)
        poiSearch.searchPOIAsyn()
    }

    fun moveToSelect(latLonPoint: LatLonPoint) { // 不搜索，只移动
        isNeedQuery = false
        val latLng = LatLng(latLonPoint.latitude, latLonPoint.longitude)
        getMap().animateCamera(CameraUpdateFactory.changeLatLng(latLng), 600, null)
    }


    override fun onPoiSearched(poiResult: PoiResult?, rCode: Int) {
        lock = false
        // fixIt
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (poiResult?.query != null) {
                when (poiResult.query) {
                    currentCenterQuery -> dealCenterQuery(poiResult) // 一个新的搜索
                    loadMoreQuery -> dealLoadMoreQuery(poiResult)
                }
            } else {
                Log.e(TAG, "onPoiSearched: ")
            }
        }
    }

    val TAG = "GetLocationOperator"

    private fun dealLoadMoreQuery(poiResult: PoiResult) {
        listener.sourceCome(buildItem(poiResult), true)
    }

    private fun dealCenterQuery(poiResult: PoiResult) {
        val data: MutableList<CheckModel> = buildItem(poiResult)
        listener.sourceCome(data, false)
    }

    private fun buildItem(poiResult: PoiResult): MutableList<CheckModel> {
        val poiItems: List<PoiItem> = poiResult.pois // 取得第一页的poiitem数据，页数从数字0开始
        val data: MutableList<CheckModel> = ArrayList()
        if (poiItems.isNotEmpty()) {
            for (poiItem in poiItems) {
                val locationModel = LocationModel(poiItem.latLonPoint).apply {
                    placeTitle = poiItem.title
                    details = buildDetailsLocation(poiItem.snippet)
                }
                data.add(CheckModel(locationModel))
            }
        }
        return data
    }

    private fun buildDetailsLocation(s: String): String {
        return searchScope.toString() + "m | $s"
    }


}
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
import com.example.amapusage.model.LocationViewModel
import com.example.amapusage.search.CheckModel

object GetLocationOperator : AMapOperator() {


    private var page: Int = 0
    private lateinit var model: LocationViewModel
    val TAG = "GetLocationOperator"
    private var lock = false
    var isNeedQuery = true
    private var searchScope = 100
    private lateinit var currentCenterQuery: PoiSearch.Query
    private lateinit var currentCenterPoint: LatLonPoint
    private lateinit var loadMoreQuery: PoiSearch.Query
    private var searchByText: PoiSearch.Query? = null

    fun bindModel(model: LocationViewModel) = apply { this.model = model }

    fun moveToSelect(latLonPoint: LatLonPoint) {
        isNeedQuery = false
        val latLng = LatLng(latLonPoint.latitude, latLonPoint.longitude)
        getMap().animateCamera(CameraUpdateFactory.changeLatLng(latLng), 600, null)
    }

    override fun onCameraChangeFinish(cameraPosition: CameraPosition) {
        super.onCameraChangeFinish(cameraPosition)
        if (isNeedQuery) { // 自动搜索的，移动到屏幕中心.
            listener.startLoadData()
            val target = cameraPosition.target
            queryByMove(LatLonPoint(target.latitude, target.longitude))
        } else if (!isNeedQuery) {
            isNeedQuery = !isNeedQuery
        }
    }

    override fun queryByText(queryText: String) {
        if (lock) return
        lock = true
        searchByText = PoiSearch.Query(queryText, "", currentLocation?.city)
        searchByText!!.pageSize = 20
        searchByText!!.pageNum = 0
        val poiSearch = PoiSearch(context, searchByText)
        poiSearch.setOnPoiSearchListener(this)
        poiSearch.query.isDistanceSort = true
        poiSearch.searchPOIAsyn()
    }

    fun loadMoreInSearch() {
        if (lock) return
        if (searchByText == null) return
        lock = true
        searchByText?.pageNum = ++page
        val poiSearch = PoiSearch(context, currentCenterQuery)
        poiSearch.setOnPoiSearchListener(this)
        poiSearch.searchPOIAsyn()
    }

    private fun queryByMove(latLonPoint: LatLonPoint) {
        if (lock) return
        lock = true
        currentCenterPoint = latLonPoint
        currentCenterQuery = PoiSearch.Query("", "", "")
        currentCenterQuery.pageNum = 0
        currentCenterQuery.pageSize = 20
        currentCenterQuery.isDistanceSort = true
        val poiSearch = PoiSearch(context, currentCenterQuery)
        poiSearch.setOnPoiSearchListener(this)
        poiSearch.bound = PoiSearch.SearchBound(latLonPoint, 1000000000)
        poiSearch.searchPOIAsyn()
    }

    fun loadMoreInCurrent() {
        if (lock) return
        lock = true
        currentCenterQuery.pageNum = ++page
        val poiSearch = PoiSearch(context, currentCenterQuery)
        poiSearch.setOnPoiSearchListener(this)
        poiSearch.bound = PoiSearch.SearchBound(currentCenterPoint, 100000000)
        poiSearch.searchPOIAsyn()
    }


    override fun onPoiSearched(poiResult: PoiResult?, rCode: Int) {
        lock = false
        // fixIt
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (poiResult?.query != null) {
                when (poiResult.query) {
                    currentCenterQuery -> dealCenterQuery(poiResult) // 一个新的搜索
                    searchByText -> dealQueryByText(poiResult)
                }
            } else {
                Log.e(TAG, "onPoiSearched: ")
            }
        } else {
            Log.e(TAG, "onPoiSearched: " + "load fail")
        }
        listener.loadDataDone()
    }

    private fun dealQueryByText(poiResult: PoiResult) {
        val data: MutableList<CheckModel> = buildItem(poiResult)
        model.searchModelList.value = data
    }


    private fun dealCenterQuery(poiResult: PoiResult) {
        val data: MutableList<CheckModel> = buildItem(poiResult)
        var value = model.currentModelList.value
        if (poiResult.query.pageNum == 0) value = data
        else value?.addAll(data)
        model.currentModelList.value = value
        if (data.size > 0) model.currentModelList.value!![0].isChecked = true
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
package com.example.amapusage.factory

import android.util.Log
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.example.amapusage.model.LocationViewModel
import com.example.amapusage.search.CheckModel

object GetLocationOperator : AMapOperator() {


    private lateinit var centerSearcher: PoiSearch
    private lateinit var byTextSearcher: PoiSearch
    private lateinit var model: LocationViewModel
    val TAG = "GetLocationOperator"
    var lock = false // 防止不停的下拉，导致页码变化
        private set
    var isNeedQuery = true
    private lateinit var currentCenterQuery: PoiSearch.Query
    private lateinit var currentCenterPoint: LatLonPoint
    private var searchByText: PoiSearch.Query? = null
    private const val searchType = "190403|190100|190400|190600|190000|170204|" +
            "050000|060000|070000|120000|180000" +
            "|080000|090000|100000|110000|130000" +
            "|140000|150000|170000|190000" +
            "|200000|210000|220000|010000|020000|160000"

    fun bindModel(model: LocationViewModel) = apply { this.model = model }
    class Node(var last: Node?, var latLonPoint: LatLonPoint) {
        fun action() {
            isNeedQuery = false
            val latLng = LatLng(latLonPoint.latitude, latLonPoint.longitude)
            getMap().animateCamera(
                CameraUpdateFactory.changeLatLng(latLng),
                600,
                object : AMap.CancelableCallback {
                    override fun onFinish() {
                        last?.action()
                        tail = last
                    }

                    override fun onCancel() {
                        last?.action()
                        tail = last
                    }
                })
        }
    }

    var tail: Node? = null // 如果checkList 发生暴击，在动画化没有完成前变成同步，导致数据错乱，但不能阻塞.

    fun moveToSelect(latLonPoint: LatLonPoint) {
        if (tail == null) {
            tail = Node(null, latLonPoint)
            tail!!.action()
        } else {
            Node(tail, latLonPoint)
        }
    }

    override fun onCameraChange(cameraPosition: CameraPosition?) {
        super.onCameraChange(cameraPosition)
        if (lock) return
        if (isNeedQuery) {
            lock = true
            listener.startLoadNewData()
        }
    }

    override fun onCameraChangeFinish(cameraPosition: CameraPosition) {
        super.onCameraChangeFinish(cameraPosition)
        if (isNeedQuery) { // 自动搜索的，移动到屏幕中心.
            val target = cameraPosition.target
            queryByMove(LatLonPoint(target.latitude, target.longitude))
        } else if (!isNeedQuery) {
            isNeedQuery = !isNeedQuery
        }
    }

    override fun queryByText(queryText: String) {
        searchByText = PoiSearch.Query(queryText, "", myLocation?.city)
        searchByText!!.pageSize = 20
        searchByText!!.pageNum = 0
        byTextSearcher = PoiSearch(context, searchByText)
        byTextSearcher.setOnPoiSearchListener(this)
        byTextSearcher.query.isDistanceSort = true
        byTextSearcher.searchPOIAsyn()
    }

    fun loadMoreByText() {
        if (lock) return
        if (searchByText == null) return
        lock = true
        byTextSearcher.query.pageNum +=1
        byTextSearcher.searchPOIAsyn()
    }

    private fun queryByMove(latLonPoint: LatLonPoint) {
        currentCenterPoint = latLonPoint
        currentCenterQuery = PoiSearch.Query("", "", myLocation?.city)
        currentCenterQuery.pageNum = 1
        currentCenterQuery.pageSize = 20
        currentCenterQuery.isDistanceSort = true
        centerSearcher = PoiSearch(context, currentCenterQuery)
        centerSearcher.setOnPoiSearchListener(this)
        centerSearcher.bound = PoiSearch.SearchBound(latLonPoint, 1000000000)
        centerSearcher.searchPOIAsyn()
    }

    fun loadMoreByMove() {
        if (lock) return
        lock = true
        centerSearcher.apply { query.pageNum += 1 }
        centerSearcher.searchPOIAsyn()
    }

    override fun onPoiSearched(poiResult: PoiResult?, rCode: Int) {
        lock = false
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
        var value = model.searchModelList.value
        if (searchByText!!.pageNum == 1) value = data
        else value?.addAll(data)
        model.searchModelList.value = value
    }

    private fun dealCenterQuery(poiResult: PoiResult) {
        val data: MutableList<CheckModel> = buildItem(poiResult)
        var value = model.currentModelList.value
        if (currentCenterQuery.pageNum == 1) {
            value = data
            if (value.size > 0) { // 默认选择第一个
                value[0].isChecked = true
                model.checkModel.value = value[0]
            }
        } else {
            value?.addAll(data)
        }
        model.currentModelList.value = value
    }

    private fun buildItem(poiResult: PoiResult): MutableList<CheckModel> {
        val poiItems: List<PoiItem> = poiResult.pois // 取得第一页的poiitem数据，页数从数字0开始
        val data: MutableList<CheckModel> = ArrayList()
        if (poiItems.isNotEmpty()) {
            for (poiItem in poiItems) {
                val checkModel = CheckModel(poiItem.latLonPoint).apply {
                    sendModel.placeTitle = poiItem.title
                    sendModel.placeDesc = poiItem.snippet
                    distanceDetails = formatDistance(poiItem.latLonPoint) + " | " + poiItem.snippet
                }
                data.add(checkModel)
            }
        }
        return data
    }

    private fun formatDistance(point: LatLonPoint): String {
        val start = LatLng(myLocation!!.latitude, myLocation!!.longitude)
        val end = LatLng(point.latitude, point.longitude)
        val distant = AMapUtils.calculateLineDistance(start, end)
        return when {
            distant < 100 -> {
                "100m内"
            }
            distant > 1000 -> {
                String.format("%.1f", (distant / 1000)) + "km"
            }
            else -> {
                distant.toInt().toString() + "m"
            }
        }
    }

}
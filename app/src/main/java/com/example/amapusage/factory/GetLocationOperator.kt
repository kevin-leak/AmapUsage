package com.example.amapusage.factory

import android.util.Log
import androidx.core.content.ContextCompat
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
import com.example.amapusage.R
import com.example.amapusage.model.LocationViewModel
import com.example.amapusage.model.CheckModel
import com.example.amapusage.utils.KeyWordUtil


class GetLocationOperator : AMapOperator() {

    private lateinit var centerSearcher: PoiSearch
    private lateinit var byTextSearcher: PoiSearch
    private lateinit var model: LocationViewModel
    val TAG = "kyle-map-GetLocation"
    var lock = false // 防止不停的下拉，导致页码变化
        private set
    var isNeedQuery = true
    private lateinit var centerQuery: PoiSearch.Query
    private lateinit var currentCenterPoint: LatLonPoint
    private var searchByText: PoiSearch.Query? = null
    private val searchType = "190403|190100|190400|190600|190000|170204|" +
            "050000|060000|070000|120000|180000" +
            "|080000|090000|100000|110000|130000" +
            "|140000|150000|170000|190000" +
            "|200000|210000|220000|010000|020000|160000"

    fun bindModel(model: LocationViewModel) = apply { this.model = model }

    inner class Node(var latLonPoint: LatLonPoint) {
        // 一个逆向的链表，先进先出，在上一个节点没有执行完，就会出现断层，如果没有执行完，就wait顺序执行.
        var last: Node? = null
        fun action() {
            isNeedQuery = false
            val latLng = LatLng(latLonPoint.latitude, latLonPoint.longitude)
            getMap().animateCamera(CameraUpdateFactory.changeLatLng(latLng), 600,
                object : AMap.CancelableCallback {
                    override fun onFinish() = turn()
                    override fun onCancel() = turn()
                })
        }

        private fun turn() {
            last?.action()
            this@GetLocationOperator.tail = last
        }
    }

    var tail: Node? = null // 如果checkList 发生暴击，在动画化没有完成前变成同步，导致数据错乱，但不能阻塞.

    fun moveToPosition(latLonPoint: LatLonPoint) {
        if (tail == null) tail = Node(latLonPoint).also { it.action() }
        else Node(latLonPoint).apply { this.last = tail }
    }

    fun moveToCheck() {
        if (!model.isChecked()) return
        moveToPosition(model.checkModel.value!!.lonPoint)
    }

    override fun onCameraChange(cameraPosition: CameraPosition?) {
        super.onCameraChange(cameraPosition)
        if (lock || !isNeedQuery) return
        lock = true
        listener.startLoadNewData()
    }

    override fun onCameraChangeFinish(cameraPosition: CameraPosition) {
        super.onCameraChangeFinish(cameraPosition)
        if (isNeedQuery) {
            val target = cameraPosition.target
            queryByMove(LatLonPoint(target.latitude, target.longitude)) // 查询数据
        } else if (!isNeedQuery) {
            isNeedQuery = !isNeedQuery
        }
    }

    override fun queryByText(queryText: String) {
        searchByText = PoiSearch.Query(queryText, "", "")
        searchByText!!.cityLimit = true
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
        byTextSearcher.query.pageNum += 1
        byTextSearcher.searchPOIAsyn()
    }

    private fun queryByMove(latLonPoint: LatLonPoint) {
        currentCenterPoint = latLonPoint
        centerQuery = PoiSearch.Query("", searchType)
        centerQuery.pageNum = 1
        centerQuery.pageSize = 20
        centerQuery.isDistanceSort = true
        centerSearcher = PoiSearch(context, centerQuery)
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
        if (rCode != AMapException.CODE_AMAP_SUCCESS || poiResult?.query == null) {
            Log.e(TAG, "onPoiSearched is fail")
            return
        }
        when (poiResult.query) {
            centerQuery -> dealCenterQuery(poiResult)
            searchByText -> dealQueryByText(poiResult)
        }
        listener.loadDataDone()
    }

    private fun dealQueryByText(poiResult: PoiResult) {
        val data: MutableList<CheckModel> = buildItem(poiResult, true)
        var value = model.searchList.value
        if (searchByText!!.pageNum == 1) value = data
        else value?.addAll(data)
        model.searchList.value = value
    }

    override fun initAction() {
        super.initAction()
        model.myLocation = myLocation
    }

    private fun dealCenterQuery(poiResult: PoiResult) {
        val data: MutableList<CheckModel> = buildItem(poiResult)
        var value = model.normalList.value
        if (centerQuery.pageNum == 1) value = data
        else value?.addAll(data)
        model.normalList.value = value
        if (centerQuery.pageNum == 1) model.setDefaultCheck(currentCenterPoint)
    }

    private fun buildItem(result: PoiResult, isSearch: Boolean = false): MutableList<CheckModel> {
        val items: List<PoiItem> = result.pois // 取得第一页的poiitem数据，页数从数字0开始
        val data: MutableList<CheckModel> = ArrayList()
        val keyword = result.query.queryString
        if (items.isEmpty()) return data
        items.forEach {
            val checkModel = CheckModel(it.latLonPoint).apply {
                distanceDetails = formatDistance(it.latLonPoint) + " | " + it.snippet
                this.isSearch = isSearch
            }
            checkModel.sendModel.apply {
                placeDesc = it.snippet
                placeTitle = if (result.query == centerQuery) it.title
                else KeyWordUtil.buildSearchKey(it.title, keyword, context)
            }
            data.add(checkModel)
        }
        return data
    }

    private fun formatDistance(point: LatLonPoint): String {
        val start = LatLng(myLocation!!.latitude, myLocation!!.longitude)
        val end = LatLng(point.latitude, point.longitude)
        val distant = AMapUtils.calculateLineDistance(start, end)
        return when {
            distant < 100 -> "100m内"
            distant > 1000 -> String.format("%.1f", (distant / 1000)) + "km"
            else -> distant.toInt().toString() + "m"
        }
    }
}
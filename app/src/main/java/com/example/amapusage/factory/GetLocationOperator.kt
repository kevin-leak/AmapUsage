package com.example.amapusage.factory

import android.util.Log
import androidx.core.content.ContextCompat
import com.amap.api.location.AMapLocation
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
import java.util.*
import kotlin.collections.ArrayList


class GetLocationOperator : AMapOperator() {

    private lateinit var centerSearcher: PoiSearch
    private lateinit var byTextSearcher: PoiSearch
    private lateinit var model: LocationViewModel
    val TAG = "kyle-map-GetLocation"
    var lock = false // 防止不停的下拉，导致页码变化
        private set
    var isNeedQuery = false
    private lateinit var centerQuery: PoiSearch.Query
    private lateinit var currentCenterPoint: LatLonPoint
    private var searchByText: PoiSearch.Query? = null
    private val searchType = "190403|190100|190400|190600|190000|170204|" +
            "050000|060000|070000|120000|180000" +
            "|080000|090000|100000|110000|130000" +
            "|140000|150000|170000|190000" +
            "|200000|210000|220000|010000|020000|160000"

    fun bindModel(model: LocationViewModel) = apply { this.model = model }

    override fun initAction() {
        isNeedQuery = true
        super.initAction()
    }

    private val moveQueue = LinkedList<LatLonPoint>()
    fun emptyAnimationQueue() {
        moveQueue.clear()
    }

    private fun moveToPosition(latLonPoint: LatLonPoint) {
        if (moveQueue.size == 0) {
            moveQueue.offer(latLonPoint)
            performMove(latLonPoint)
        } else {
            moveQueue.offer(latLonPoint)
        }
    }

    private fun performMove(latLonPoint: LatLonPoint) {
        isNeedQuery = false
        val latLng = LatLng(latLonPoint.latitude, latLonPoint.longitude)
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16f))
        getMap().animateCamera(CameraUpdateFactory.changeLatLng(latLng), 600, object :
            AMap.CancelableCallback {
            override fun onFinish() {
                moveQueue.poll()
                if (moveQueue.size >= 1) performMove(moveQueue.poll()!!)
            }

            override fun onCancel() {
                moveQueue.poll()
                if (moveQueue.size >= 1) performMove(moveQueue.poll()!!)
            }
        })
    }

    fun moveToCheck() {
        if (!model.isChecked()) return
        moveToPosition(model.checkModel.value!!.lonPoint)
    }


    override fun onCameraChangeFinish(cameraPosition: CameraPosition) {
        super.onCameraChangeFinish(cameraPosition)
        if (lock || !isNeedQuery) return // 非查询的不调用 startLoadNewData()
        lock = true
        listener.startLoadNewData()
        val target = cameraPosition.target
        queryByMove(LatLonPoint(target.latitude, target.longitude)) // 查询数据
    }

    override fun queryByText(queryText: String) {
        searchByText = PoiSearch.Query(queryText, "", "")
        searchByText!!.cityLimit = true
        searchByText!!.pageSize = 20
        searchByText!!.pageNum = 1
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
        if (!this::centerSearcher.isInitialized) return
        centerSearcher.query.pageNum += 1
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

    override fun locationCome(aMapLocation: AMapLocation?) {
        model.myLocation = aMapLocation
        super.locationCome(aMapLocation)
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
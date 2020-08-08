package com.example.amapusage.factory

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
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
import com.example.amapusage.search.CheckModel
import com.example.amapusage.utils.KeyWordUtil
import java.util.regex.Matcher
import java.util.regex.Pattern


class GetLocationOperator : AMapOperator() {

    private lateinit var centerSearcher: PoiSearch
    private lateinit var byTextSearcher: PoiSearch
    private lateinit var model: LocationViewModel
    val TAG = "kyle-map-GetLocation"
    var lock = false // 防止不停的下拉，导致页码变化
        private set
    var isNeedQuery = true
    private lateinit var currentCenterQuery: PoiSearch.Query
    private lateinit var currentCenterPoint: LatLonPoint
    private var searchByText: PoiSearch.Query? = null
    private val searchType = "190403|190100|190400|190600|190000|170204|" +
            "050000|060000|070000|120000|180000" +
            "|080000|090000|100000|110000|130000" +
            "|140000|150000|170000|190000" +
            "|200000|210000|220000|010000|020000|160000"

    fun bindModel(model: LocationViewModel) = apply { this.model = model }
    class Node(var latLonPoint: LatLonPoint) {
        var last: Node? = null
        fun action(operator: GetLocationOperator) {
            operator.isNeedQuery = false
            val latLng = LatLng(latLonPoint.latitude, latLonPoint.longitude)
            operator.getMap().animateCamera(CameraUpdateFactory.changeLatLng(latLng), 600,
                object : AMap.CancelableCallback {
                    override fun onFinish() {
                        last?.action(operator)
                        operator.tail = last
                    }

                    override fun onCancel() {
                        last?.action(operator)
                        operator.tail = last
                    }
                })
        }
    }

    var tail: Node? = null // 如果checkList 发生暴击，在动画化没有完成前变成同步，导致数据错乱，但不能阻塞.

    // 一个逆向的链表，先进先出，在上一个节点没有执行完，就会出现断层，如果没有执行完，就wait顺序执行.
    fun moveToSelect(latLonPoint: LatLonPoint) {
        if (tail == null) {
            tail = Node(latLonPoint)
            tail!!.action(this)
        } else {
            Node(latLonPoint).apply { last = tail }
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
        if (isNeedQuery) { // 自动搜索的，移动到屏幕中心. 第二个运行到这里
            val target = cameraPosition.target
            queryByMove(LatLonPoint(target.latitude, target.longitude)) // 查询数据
        } else if (!isNeedQuery) {
            isNeedQuery = !isNeedQuery // 选中，第一个运行到这
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
            if (poiResult?.query != null) {
                Log.e(TAG, "onPoiSearched: " + "load fail")
            }
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

    private fun dealCenterQuery(poiResult: PoiResult) {
        val data: MutableList<CheckModel> = buildItem(poiResult)
        var value = model.normalList.value
        if (currentCenterQuery.pageNum == 1) {
            value = data
            if (value.size > 0) { // 默认选择第一个
                value[0].isChecked = true
                model.checkModel.value = value[0]
            }
        } else {
            value?.addAll(data)
        }
        model.normalList.value = value
    }

    private fun buildItem(
        poiResult: PoiResult,
        isSearch: Boolean = false
    ): MutableList<CheckModel> {
        val poiItems: List<PoiItem> = poiResult.pois // 取得第一页的poiitem数据，页数从数字0开始
        val data: MutableList<CheckModel> = ArrayList()
        val keyword = poiResult.query.queryString
        if (poiItems.isNotEmpty()) {
            for (poiItem in poiItems) {
                val checkModel = CheckModel(poiItem.latLonPoint).apply {
                    sendModel.placeTitle = if (poiResult.query == currentCenterQuery) poiItem.title
                    else KeyWordUtil.matcherSearchTitle(
                        ContextCompat.getColor(context, R.color.searchKey),
                        poiItem.title,
                        keyword
                    )
                    sendModel.placeDesc = poiItem.snippet
                    distanceDetails = formatDistance(poiItem.latLonPoint) + " | " + poiItem.snippet
                    this.isSearch = isSearch
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
            distant < 100 -> "100m内"
            distant > 1000 -> String.format("%.1f", (distant / 1000)) + "km"
            else -> distant.toInt().toString() + "m"
        }
    }
}
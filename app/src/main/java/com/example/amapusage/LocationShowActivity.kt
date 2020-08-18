

package com.example.amapusage

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.example.amapusage.MainActivity.Companion.RESULT_BITMAP
import com.example.amapusage.MainActivity.Companion.RESULT_CODE_SEND_MODEL
import com.example.amapusage.MainActivity.Companion.RESULT_SEND_MODEL
import com.example.amapusage.collapse.IScrollSensor
import com.example.amapusage.factory.GetLocationOperator
import com.example.amapusage.factory.IMapOperator
import com.example.amapusage.model.CheckModel
import com.example.amapusage.model.LocationViewModel
import com.example.amapusage.search.EntityCheckAdapter
import com.example.amapusage.search.EntityCheckSearch
import com.example.amapusage.search.IEntityCheckSearch
import com.example.amapusage.utils.BitmapUtils
import com.example.amapusage.utils.KeyBoardUtils
import kotlinx.android.synthetic.main.activity_show_location.*
import java.io.ByteArrayOutputStream
import java.util.*


class LocationShowActivity : LocationActivity(), IMapOperator.LocationSourceLister,
    IScrollSensor.CollapsingListener, IMapOperator.LocateCurrentState {
    private lateinit var operator: GetLocationOperator
    private lateinit var checkAdapter: EntityCheckAdapter
    private lateinit var viewModel: LocationViewModel
    private val searchQueue = LinkedList<String>()

    companion object {
        const val TAG = "kyle-map-MapShow"
    }

    fun LinkedList<String>.offerAndSearch(text: String) {
        when (this.size) { // 必须是大小为2不然无法判断是否正在执行.
            0 -> {
                offer(text)
                executeQuery(text)
            }
            1 -> {
                offer(text)
            }
            else -> {
                pollLast()
                offer(text)
            }
        }
    }

    private fun LinkedList<String>.pollAndSearch() {
        pollFirst()
        if (size == 1) executeQuery(pollLast()!!)
    }

    private fun LinkedList<String>.retrySearch() {
        val value = peekFirst()
        if (value != null) executeQuery(value)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        textureMapView.onCreate(savedInstanceState) // 此方法必须重写
        sensor.bindCollapsingView(textureMapView)
    }

    override fun initData() {
        super.initData()
        initViewModel()
        initOperator()
        initAdapter()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun registerListener() {
        super.registerListener()
        sensor.setCollapsingListener(this)
        collapseButtonLayout.setOnTouchListener { _, _ ->
            sensor.changeCollapseState(false)
            true
        }
        collapseButton.setOnTouchListener { _, _ ->
            sensor.changeCollapseState(false)
            true
        }
        searchView.addSearchListener(object : EntityCheckSearch.OnSearchListenerIml() {
            override fun onEnterModeChange(isEnter: Boolean) = sensor.changeCollapseState(isEnter)
            override fun sourceChanging(data: String) = searchQueue.offerAndSearch(data)
            override fun onSearchModeChange(isSearch: Boolean) = changeState(isSearch)
        })
        textureMapView.map.setOnMapTouchListener { // collapsed下不可滑动
            if (sensor.isCollapsing) sensor.changeCollapseState(false)
            operator.isNeedQuery = !searchView.isSearch  // search状态不移动查询.
        }
        recycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(v: RecyclerView, s: Int) = loadMore()
        })
    }

    override fun getResourcesId(): Int = R.layout.activity_show_location

    override fun netStateChange(hasNetwork: Boolean) {
        if (hasNetwork) {
            operator.restartClient()
            if (searchView.isSearch) searchQueue.retrySearch()
        }
    }

    override fun gpsStateChange(haveGspPermission: Boolean) {
        if (haveGspPermission) {
            operator.restartClient()
        }
    }

    private fun initOperator() {
        operator = GetLocationOperator()
        operator.preWork(textureMapView, this)
        operator.bindModel(viewModel).bindCurrentButton(currentLocationButton, this)
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)
        viewModel.checkModel.observe(this, Observer {
            if (it == null) changeSendButtonActive(false)
            else changeSendButtonActive(it.isSearch == searchView.isSearch)
        })
        viewModel.searchList.observe(this, Observer<MutableList<CheckModel>> { checkAdapter.notifyDataSetChanged() })
        viewModel.normalList.observe(this, Observer<MutableList<CheckModel>> { checkAdapter.notifyDataSetChanged() })
    }

    private fun loadMore() {
        if (recycleView.canScrollVertically(1)) return
        if (searchView.isEnterMode) operator.loadMoreByText()
        else operator.loadMoreByMove()
        checkAdapter.addFootItem()
    }

    private fun changeState(isSearch: Boolean) {
        clearDataStatus()
        if (isSearch) resetSearchState()
        else resetNormalState()
    }

    private fun resetNormalState() {
        operator.clearPositionMark()
        operator.clearAllMarkerBase()
        checkAdapter.switchData(viewModel.normalList)
        val snapshot = checkAdapter.restoreSnapshot() // 如果没有网络的情况下进入search再退出
        if (snapshot != -1) recycleView.smoothScrollToPosition(snapshot)
        operator.isNeedCenterPin = true
        operator.setUpCenterMark()
    }

    private fun resetSearchState() {
        operator.emptyAnimationQueue()
        operator.isNeedCenterPin = false
        checkAdapter.takeASnapshot()
        operator.clearCenterMark()
        operator.clearPositionMark()
        operator.clearAllMarkerBase()
        viewModel.resetSearch()
        checkAdapter.switchData(viewModel.searchList)
    }

    private fun executeQuery(data: String) {
        progressBar.visibility = if (!TextUtils.isEmpty(data)) VISIBLE else GONE
        if (!TextUtils.isEmpty(data)) {
            operator.queryByText(data)
        } else {
            clearDataStatus()
            viewModel.resetSearch()
            searchQueue.pollLast() // 自动删除，因为不会loadDone
        }
    }

    private fun clearDataStatus() {
        textPlaceHolder.visibility = GONE
        progressBar.visibility = GONE
        checkAdapter.removeFootItem()
        searchQueue.clear()
    }

    private fun initAdapter() {
        recycleView.layoutManager = LinearLayoutManager(this) //线性
        checkAdapter = EntityCheckAdapter(this, viewModel)
        recycleView.adapter = checkAdapter
        checkAdapter.listener = object : IEntityCheckSearch.CheckListener {
            override fun hasBeChecked(position: Int) = checkAnimation()
        }
    }

    private fun checkAnimation() {
        operator.moveToCheck()
        if (!searchView.isSearch) return
        val value = viewModel.checkModel.value!!
        val lang = LatLng(value.lonPoint.latitude, value.lonPoint.longitude)
        operator.removeAddPositionMarker(lang)
    }

    override fun performLocateCurrent(isOnAnimation: Boolean): Boolean {
        var isConsume = false
        if (!isOnAnimation) {
            isConsume = checkAdapter.checkCurrent()
            if (!isConsume) {
                operator.isNeedQuery = !searchView.isSearch
                operator.moveToCurrent()
            } else {
                recycleView.smoothScrollToPosition(0) // 如果是check过去的，移动到顶端。
            }
        }
        if (searchView.isEnterMode) sensor.changeCollapseState(false)
        return isConsume
    }

    override fun startLoadNewData() { // search状态下
        if (searchView.isSearch) return
        progressBar.visibility = VISIBLE
        viewModel.resetNormal()
    }

    override fun loadDataDone() {
        placeHolderReversalState()
        progressBar.visibility = GONE
        checkAdapter.removeFootItem()
        if (searchView.isSearch) searchQueue.pollAndSearch()
    }

    private fun placeHolderReversalState() {
        if (!viewModel.isChecked() && viewModel.searchList.value!!.size <= 0 &&
            !TextUtils.isEmpty(searchView.text) && searchView.isSearch
        ) {
            textPlaceHolder.visibility = VISIBLE
        } else {
            textPlaceHolder.visibility = GONE
        }
        if (searchView.isSearch && viewModel.searchList.value != null
            && viewModel.searchList.value!!.size >= 0
            && TextUtils.isEmpty(searchView.text)
        ) {
            viewModel.searchList.value = mutableListOf()
            textPlaceHolder.visibility = GONE
        }
    }

    private fun changeSendButtonActive(isClickable: Boolean) {
        sendLocationButton.isClickable = isClickable
        val colorId = if (isClickable) ContextCompat.getColor(this, R.color.white)
        else ContextCompat.getColor(this, R.color.sendButtonTextImActive)
        sendLocationButton.setTextColor(colorId)
        val tmp = if (isClickable) R.drawable.shape_send_clickable
        else R.drawable.shape_send_unclikable
        sendLocationButton.background = ContextCompat.getDrawable(this, tmp)
    }

    fun onSendLocation(view: View) {
        if (viewModel.isChecked() && view.isClickable) {
            operator.aMap.getMapScreenShot(object : AMap.OnMapScreenShotListener {
                override fun onMapScreenShot(bitmap: Bitmap) {
                    Intent().run {
                        putExtra(RESULT_SEND_MODEL, viewModel.checkModel.value!!.sendModel)
                        putExtra(RESULT_BITMAP, buildSuitableBitmap(bitmap))
                        setResult(RESULT_CODE_SEND_MODEL, this)
                    }
                    finish()
                }
                override fun onMapScreenShot(p0: Bitmap?, p1: Int) {}
            })
        }
    }

    private fun buildSuitableBitmap(bitmap: Bitmap): ByteArray {
        val bit = BitmapUtils.createScaledBitmap(bitmap, 100, 50)
        val bao = ByteArrayOutputStream()
        bit.compress(Bitmap.CompressFormat.PNG, 100, bao)
        val bitmapByte: ByteArray = bao.toByteArray()
        bao.close()
        return bitmapByte
    }

    override fun beforeCollapseStateChange(isCollapsing: Boolean) {
        if (isCollapsing) KeyBoardUtils.closeKeyboard(searchView.windowToken, baseContext)
        resetCenterMark()
    }

    override fun collapseStateChanged(isCollapsed: Boolean) {
        operator.getMap().uiSettings.isScaleControlsEnabled = !isCollapsed
        collapseButtonLayout.apply { visibility = if (isCollapsed) VISIBLE else GONE }
        resetCenterMark()
        if (searchView.isSearch && !isCollapsed) {
            val index = checkAdapter.getPosition()
            if (index != -1) recycleView.smoothScrollToPosition(index)
        }
    }

    override fun onBackPressed() {
        when {
            searchView.isSearch -> searchView.exitSearchMode()
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        operator.endOperate()
        textureMapView.onDestroy()
        KeyBoardUtils.closeKeyboard(searchView.windowToken, baseContext)
        super.onDestroy()
    }

    private fun resetCenterMark() {
        if (!searchView.isSearch) operator.resetCenterMark()
    }
    override fun moveCameraFinish() = resetCenterMark()
    override fun onMoveChange() {}
    override fun onCollapseStateChange(isCollapsed: Boolean) = resetCenterMark()
    override fun onResume() = super.onResume().also { textureMapView.onResume() }
    override fun onPause() = super.onPause().also { textureMapView.onPause() }
    fun outMap(v: View) = finish()
}

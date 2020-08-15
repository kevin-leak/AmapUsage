package com.example.amapusage

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
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
import com.example.amapusage.utils.ScreenUtils
import kotlinx.android.synthetic.main.activity_show_location.*
import java.io.ByteArrayOutputStream
import java.util.*


class LocationShowActivity : AppCompatActivity(), IMapOperator.LocationSourceLister,
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

    private fun LinkedList<String>.pollAndSearch(){
        if (searchView.isSearch) {
            pollFirst()
            if (size == 1) executeQuery(pollLast()!!)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenUtils.setStatus(this)
        setContentView(R.layout.activity_show_location)
        textureMapView.onCreate(savedInstanceState) // 此方法必须重写
        sensor.bindCollapsingView(textureMapView)
        initViewModel()
        initAdapter()
        initOperator()
        initListener()
    }

    private fun initOperator() {
        operator = GetLocationOperator()
        operator.preWork(textureMapView, this)
        operator.bindModel(viewModel).bindCurrentButton(currentLocationButton, this)
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)
        viewModel.checkModel.observe(this, Observer { changeSendButtonActive(it != null) })
        viewModel.searchList.observe(
            this,
            Observer<MutableList<CheckModel>> { checkAdapter.notifyDataSetChanged() })
        viewModel.normalList.observe(
            this,
            Observer<MutableList<CheckModel>> { checkAdapter.notifyDataSetChanged() })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
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
            override fun sourceCome(data: String) = operator.markAllDataBase()
            override fun onSearchModeChange(isSearch: Boolean) = changeState(isSearch)
        })
        textureMapView.map.setOnMapTouchListener { // collapsed下不可滑动
            if (sensor.isCollapsing) sensor.changeCollapseState(false)
            if (searchView.isSearch) operator.isNeedQuery = false  // search状态不移动查询.
        }
        entityRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(v: RecyclerView, s: Int) = loadMore()
        })
    }

    private fun loadMore() {
        if (entityRecycleView.canScrollVertically(1)) return
        if (searchView.isEnterMode) operator.loadMoreByText()
        else operator.loadMoreByMove()
        checkAdapter.addFootItem()
    }

    private fun changeState(isSearch: Boolean) {
        clearSearchStatus()
        if (isSearch) resetSearchState()
        else resetNormalState()
    }

    private fun resetNormalState() {
        operator.setUpCenterMark()
        viewModel.restoreSnapshot()
        operator.moveToCheck()
        entityRecycleView.scrollToPosition(viewModel.snapshot)
        checkAdapter.switchData(viewModel.normalList)
    }

    private fun resetSearchState() {
        operator.clearCenterMark()
        operator.clearPositionMark()
        viewModel.takeASnapshot()
        viewModel.resetSearch()
        checkAdapter.switchData(viewModel.searchList)
    }

    private fun executeQuery(data: String) {
        progressBar.visibility = if (!TextUtils.isEmpty(data)) VISIBLE else GONE
        if (!TextUtils.isEmpty(data)) {
            operator.queryByText(data)
        } else {
            clearSearchStatus()
            viewModel.resetSearch()
            searchQueue.pollLast() // 自动删除，因为不会loadDone
        }
    }

    private fun clearSearchStatus() {
        textPlaceHolder.visibility = GONE
        progressBar.visibility = GONE
    }

    private fun initAdapter() {
        entityRecycleView.layoutManager = LinearLayoutManager(this) //线性
        checkAdapter = EntityCheckAdapter(this, viewModel)
        entityRecycleView.adapter = checkAdapter
        checkAdapter.listener = object : IEntityCheckSearch.CheckListener {
            override fun hasBeChecked(position: Int) = checkLinkAnimation()
        }
    }

    private fun checkLinkAnimation() {
        val value = viewModel.checkModel.value!!
        operator.moveToCheck()
        if (!searchView.isSearch) return
        val lang = LatLng(value.lonPoint.latitude, value.lonPoint.longitude)
        operator.addPositionMarker(lang)
    }

    override fun performLocate(isOnAnimation: Boolean) {
        if (isOnAnimation){
            if (!checkAdapter.checkCurrent()) operator.moveToCurrent()
            else entityRecycleView.smoothScrollToPosition(0)
            if (searchView.isSearch) operator.setUpCenterMark() // 因为不对数据进行查询，所以不存在center.
        }
        if (searchView.isEnterMode) sensor.changeCollapseState(false)
    }

    override fun startLoadNewData() {
        progressBar.visibility = VISIBLE
        viewModel.reSetAllData()
    }

    override fun loadDataDone() {
        placeHolderReversalState()
        progressBar.visibility = GONE
        checkAdapter.removeFootItem()
        searchQueue.pollAndSearch()
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
                    searchView.exitEditMode()
                    textureMapView.onDestroy()
                    operator.endOperate()
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
    }

    override fun collapseStateChanged(isCollapsed: Boolean) {
        operator.getMap().uiSettings.isScaleControlsEnabled = !isCollapsed
        collapseButtonLayout.apply {
            visibility = if (isCollapsed) VISIBLE else GONE
//            animation = AlphaAnimation(if (isCollapsed) 0f else 1f, if (isCollapsed) 1f else 0f)
//            animation.duration = 10
//            animation.fillAfter = true
//            animation.interpolator = AccelerateInterpolator()
//            animation.start()
        }
        if (!searchView.isSearch) operator.resetCenterMark()
    }

    override fun onBackPressed() {
        if (searchView.isEnterMode) searchView.exitEditMode()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        operator.endOperate()
        textureMapView.onDestroy()
        KeyBoardUtils.closeKeyboard(searchView.windowToken, baseContext)
        super.onDestroy()
    }

    override fun moveCameraFinish() {}
    override fun onMoveChange() {}
    override fun onCollapseStateChange(isCollapsed: Boolean) =
        run { if (!searchView.isSearch) operator.resetCenterMark() }

    override fun onResume() = super.onResume().also { textureMapView.onResume() }
    override fun onPause() = super.onPause().also { textureMapView.onPause() }
    fun outMap(v: View) = finish()
}

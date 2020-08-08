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
import com.example.amapusage.collapse.IScrollSensor
import com.example.amapusage.factory.GetLocationOperator
import com.example.amapusage.factory.IMapOperator
import com.example.amapusage.model.LocationViewModel
import com.example.amapusage.search.*
import com.example.amapusage.utils.BitmapUtils
import com.example.amapusage.utils.KeyBoardUtils
import com.example.amapusage.utils.ScreenUtils
import kotlinx.android.synthetic.main.activity_show_location.*
import java.io.ByteArrayOutputStream


open class LocationShowActivity : AppCompatActivity(), IMapOperator.LocationSourceLister,
    IScrollSensor.CollapsingListener {
    private lateinit var operator: GetLocationOperator
    private lateinit var entityCheckAdapter: EntityCheckAdapter
    private lateinit var viewModel: LocationViewModel

    companion object {
        const val TAG = "kyle-map-MapShow"
        const val RESULT_CODE_SEND_MODEL = 200
        const val RESULT_MAP_BITMAP = "RESULT_MAP_BITMAP"
        const val RESULT_SEND_MODEL = "RESULT_Send_MODEL"
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
        operator.apply {
            bindCurrentButton(currentLocationButton)
            bindModel(viewModel)
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)
        viewModel.checkModel.observe(this, Observer {
            changeSendButtonActive(it != null)
        })
        viewModel.searchList.observe(
            this,
            Observer<MutableList<CheckModel>> { entityCheckAdapter.notifyDataSetChanged() })
        viewModel.normalList.observe(
            this,
            Observer<MutableList<CheckModel>> { entityCheckAdapter.notifyDataSetChanged() })
    }

    private fun executeQuery(data: String) {
        if (!TextUtils.isEmpty(data)) {
            progressBar.visibility = VISIBLE
            operator.queryByText(data) // 如果为空则fail
        } else {
            viewModel.searchList.value = mutableListOf()
            viewModel.checkModel.value = null
            progressBar.visibility = GONE
        }
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
        locationSearchView.addSearchListener(object :
            EntityCheckSearch.OnSearchListenerIml() {
            override fun onEnterModeChange(isEnter: Boolean) = sensor.changeCollapseState(isEnter)

            override fun sourceChanging(data: String) {
                resetSearchAdapter()
                executeQuery(data)
            }

            override fun onSearchModeChange(isSearch: Boolean) {
                textPlaceHolder.visibility = GONE
                progressBar.visibility = GONE
                if (isSearch) {
                    resetSearchAdapter()
                    entityCheckAdapter.switchData(viewModel.searchList)
                } else {
                    resetCurrentAdapter()
                    entityCheckAdapter.switchData(viewModel.normalList)
                }
            }
        })
        textureMapView.map.setOnMapTouchListener { // collapsed下不可滑动
            if (sensor.isCollapsing) sensor.changeCollapseState(false)
            if (locationSearchView.isSearch) operator.isNeedQuery = false  // search状态不移动查询.
        }
        entityRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!entityRecycleView.canScrollVertically(1)) {
                    entityCheckAdapter.addFootItem()
                    if (locationSearchView.isEnterMode) operator.loadMoreByText()
                    else operator.loadMoreByMove()
                }
            }
        })
    }

    private fun resetCurrentAdapter() {
        operator.setUpCenterMark()
        viewModel.checkModel.value = viewModel.normalList.value!![viewModel.tempInt]
        if (viewModel.checkModel.value != null) operator.moveToSelect(viewModel.checkModel.value!!.lonPoint)
    }

    private fun resetSearchAdapter() {
        operator.clearCenterMark()
        operator.clearPositionMark()
        viewModel.searchList.value = mutableListOf()
        viewModel.checkModel.value = null
        viewModel.tempInt =
            viewModel.normalList.value?.indexOf(viewModel.checkModel.value) ?: 0
        viewModel.tempInt = if (viewModel.tempInt == -1) 0 else viewModel.tempInt
    }

    private fun initAdapter() {
        entityRecycleView.layoutManager = LinearLayoutManager(this) //线性
        entityCheckAdapter = EntityCheckAdapter(this, viewModel)
        entityCheckAdapter.listener = object : IEntityCheckSearch.CheckListener {
            override fun hasBeChecked(position: Int) {
                operator.moveToSelect(viewModel.checkModel.value!!.lonPoint)
                if (locationSearchView.isSearch) {
                    val latLng = LatLng(
                        viewModel.checkModel.value!!.lonPoint.latitude,
                        viewModel.checkModel.value!!.lonPoint.longitude
                    )
                    operator.addPositionMarker(latLng)
                }
            }
        }
        entityRecycleView.adapter = entityCheckAdapter
    }


    override fun onDestroy() {
        KeyBoardUtils.closeKeyboard(locationSearchView.windowToken, baseContext)
        textureMapView.onDestroy()
        operator.endOperate()
        super.onDestroy()
    }

    fun loadCurrentLocation(view: View) {
        operator.moveToCurrent()
        if (sensor.isCollapsing && locationSearchView.isEnterMode) sensor.changeCollapseState(false)
    }

    override fun startLoadNewData() {
        progressBar.visibility = VISIBLE
        viewModel.checkModel.value = null
        viewModel.searchList.value = mutableListOf()
        viewModel.normalList.value = mutableListOf()
    }

    override fun loadDataDone() {
        progressBar.visibility = GONE
        entityCheckAdapter.removeFootItem()
        if (viewModel.searchList.value != null && viewModel.searchList.value!!.size <= 0 &&
            !TextUtils.isEmpty(locationSearchView.text) && locationSearchView.isSearch
        ) {
            textPlaceHolder.visibility = VISIBLE
        } else {
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
        if (viewModel.checkModel.value != null && sendLocationButton.isActivated) {
            operator.aMap.getMapScreenShot(object : AMap.OnMapScreenShotListener {
                override fun onMapScreenShot(bitmap: Bitmap) {
                    Intent().run {
                        putExtra(RESULT_MAP_BITMAP, buildSuitableBitmap(bitmap))
                        putExtra(RESULT_SEND_MODEL, viewModel.checkModel.value!!.sendModel)
                        setResult(RESULT_CODE_SEND_MODEL, this)
                    }
                    locationSearchView.exitEditMode()
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
        if (isCollapsing) KeyBoardUtils.closeKeyboard(locationSearchView.windowToken, baseContext)
        operator.clearCenterMark()
    }

    override fun onCollapseStateChange(isCollapsed: Boolean) {
        if (!locationSearchView.isSearch) operator.resetCenterMark()
    }

    override fun collapseStateChanged(isCollapsed: Boolean) {
        if (!isCollapsed) {
            entityRecycleView.scrollToPosition(entityCheckAdapter.checkPosition)
        }
        operator.getMap().uiSettings.isScaleControlsEnabled = !isCollapsed
        collapseButtonLayout.apply {
            visibility = if (isCollapsed) VISIBLE else GONE
//            animation = AlphaAnimation(if (isCollapsed) 0f else 1f, if (isCollapsed) 1f else 0f)
//            animation.duration = 10
//            animation.fillAfter = true
//            animation.interpolator = AccelerateInterpolator()
//            animation.start()
        }
        if (!locationSearchView.isSearch) operator.resetCenterMark()
    }

    override fun onBackPressed() {
        if (locationSearchView.isEnterMode) {
            locationSearchView.exitEditMode()
            return
        }
        super.onBackPressed()
    }

    override fun moveCameraFinish() {}
    override fun onMoveChange() {}
    override fun onResume() = super.onResume().also { textureMapView.onResume() }
    override fun onPause() = super.onPause().also { textureMapView.onPause() }
    fun outMap(v: View) = finish()
}

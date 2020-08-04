package com.example.amapusage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.AMap
import com.amap.api.maps.TextureMapView
import com.example.amapusage.collapse.IScrollSensor
import com.example.amapusage.collapse.ScrollSensorLayout
import com.example.amapusage.factory.GetLocationOperator
import com.example.amapusage.factory.IMapOperator
import com.example.amapusage.model.LocationViewModel
import com.example.amapusage.search.CheckModel
import com.example.amapusage.search.EntityCheckAdapter
import com.example.amapusage.search.EntityCheckSearch
import com.example.amapusage.search.IEntityCheckSearch
import com.example.amapusage.utils.KeyBoardUtils
import com.example.amapusage.utils.ScreenUtils
import kotlinx.android.synthetic.main.activity_show_map.*
import java.io.ByteArrayOutputStream


open class MapShowActivity : AppCompatActivity(), IMapOperator.LocationSourceLister,
    IScrollSensor.CollapsingListener {
    private lateinit var progressBar: ProgressBar
    val TAG = "MapShowActivity"
    private lateinit var entityCheckAdapter: EntityCheckAdapter
    private lateinit var currentLocationButton: ImageButton
    private lateinit var viewModel: LocationViewModel
    private lateinit var sendLocationButton: Button
    private lateinit var collapseButton: ImageButton
    private lateinit var collapseButtonLayout: RelativeLayout
    private lateinit var textureMapView: TextureMapView
    private lateinit var locationSearchView: EntityCheckSearch
    private lateinit var sensor: ScrollSensorLayout
    private lateinit var entityRecycleView: RecyclerView


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenUtils.setStatus(this)
        setContentView(R.layout.activity_show_map)
        initView(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)
        viewModel.checkModel.observe(this, Observer {
            if (it != null) {
                GetLocationOperator.setUpMapPin()
                changeSendButtonActive(true)
            } else {
                GetLocationOperator.clearMapPin() // search状态不移动
                changeSendButtonActive(false)
            }
        })
        viewModel.searchModelList.observe(this, Observer<MutableList<CheckModel>> {
            entityCheckAdapter.notifyDataSetChanged()
            entityCheckAdapter.removeFootItem()
        })
        viewModel.currentModelList.observe(this, Observer<MutableList<CheckModel>> {
            entityCheckAdapter.notifyDataSetChanged()
            entityCheckAdapter.removeFootItem()
        })
        GetLocationOperator.preWork(textureMapView, this)
            .bindCurrentButton(findViewById(R.id.current_location_button))
            .bindMapPin(findViewById(R.id.map_pin))
        GetLocationOperator.bindModel(viewModel)
        initAdapter()
        initListener()
    }

    private fun initView(savedInstanceState: Bundle?) {
        textureMapView = findViewById(R.id.texture_map_view)
        textureMapView.onCreate(savedInstanceState) // 此方法必须重写
        sensor = findViewById(R.id.scroll_collapse_sensor)
        collapseButton = findViewById(R.id.collapse_button)
        collapseButtonLayout = findViewById(R.id.collapse_button_layout)
        locationSearchView = findViewById(R.id.ls_Search_view)
        sendLocationButton = findViewById(R.id.send_location_button)
        currentLocationButton = findViewById(R.id.current_location_button)
        entityRecycleView = findViewById(R.id.entity_recycle_view)
        progressBar = findViewById(R.id.progress_bar)
        sensor.bindCollapsingView(textureMapView)
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
        locationSearchView.setSearchListener(object :
            EntityCheckSearch.OnSearchListenerIml() {
            override fun onEnterModeChange(isEnter: Boolean) { // 当重新获取焦点市要弹出
                sensor.changeCollapseState(isEnter) // search聚焦与collapse连锁
            }

            override fun sourceChanging(data: String) {
                if (!TextUtils.isEmpty(data)) {
                    progressBar.visibility = VISIBLE
                    GetLocationOperator.queryByText(data)
                } else {
                    viewModel.searchModelList.value = mutableListOf()
                    viewModel.checkModel.value = null
                    progressBar.visibility = GONE
                }
            }

            override fun onSearchModeChange(isSearch: Boolean) {
                if (isSearch) {
                    viewModel.tmp = viewModel.checkModel.value
                    entityCheckAdapter.switchData(viewModel.searchModelList)
                } else {
                    progressBar.visibility = GONE
                    viewModel.checkModel.value = viewModel.tmp
                    viewModel.tmp = null
                    GetLocationOperator.moveToSelect(viewModel.checkModel.value?.sendModel!!.latLonPoint)
                    viewModel.searchModelList.value?.clear()
                    entityCheckAdapter.switchData(viewModel.currentModelList)
                }
            }
        })
        textureMapView.map.setOnMapTouchListener { // collapsed下不可滑动
            if (sensor.isCollapsing) sensor.changeCollapseState(false)
            // search状态不移动查询.
            if (locationSearchView.isSearch) GetLocationOperator.isNeedQuery = false
        }

        entityRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!entityRecycleView.canScrollVertically(1)) {
                    entityCheckAdapter.addFootItem()
                    if (locationSearchView.isEnterMode) {
                        GetLocationOperator.loadMoreByText()
                    } else {
                        GetLocationOperator.loadMoreByMove()
                    }
                }
            }
        })
    }

    private fun initAdapter() {
        entityRecycleView.layoutManager = LinearLayoutManager(this) //线性
        entityCheckAdapter = EntityCheckAdapter(this, viewModel)
        entityCheckAdapter.listener = object : IEntityCheckSearch.CheckListener {
            override fun hasBeChecked(position: Int) {
                GetLocationOperator.moveToSelect(viewModel.checkModel.value?.sendModel!!.latLonPoint)
            }
        }
        entityRecycleView.adapter = entityCheckAdapter
    }

    override fun onDestroy() {
        KeyBoardUtils.closeKeyboard(locationSearchView.windowToken, baseContext)
        textureMapView.onDestroy()
        GetLocationOperator.endOperate()
        super.onDestroy()
    }

    fun loadCurrentLocation(view: View) {
        GetLocationOperator.moveToCurrent()
        if (sensor.isCollapsing && locationSearchView.isEnterMode) sensor.changeCollapseState(false)
    }


    override fun moveCameraFinish() {}

    override fun onMoveChange() {}

    override fun startLoadNewData() {
        progressBar.visibility = VISIBLE
        viewModel.checkModel.value = null
        viewModel.searchModelList.value?.clear()
        viewModel.currentModelList.value?.clear()
    }

    override fun loadDataDone() {
        progressBar.visibility = GONE
        entityCheckAdapter.removeFootItem()
    }

    override fun onResume() = super.onResume().also { textureMapView.onResume() }
    override fun onPause() = super.onPause().also { textureMapView.onPause() }
    fun outMap(v: View) = finish()

    private fun changeSendButtonActive(isClickable: Boolean) {
        sendLocationButton.setTextColor(Color.parseColor(if (isClickable) "#ffffff" else "#808080"))
        val tmp = if (isClickable) R.drawable.shape_send_clickable
        else R.drawable.shape_send_unclikable
        sendLocationButton.background = resources.getDrawable(tmp)
    }

    fun onSendLocation(view: View) {
        if (viewModel.checkModel.value != null) {
            GetLocationOperator.aMap.getMapScreenShot(object : AMap.OnMapScreenShotListener {
                override fun onMapScreenShot(bitmap: Bitmap?) {
                    val baos = ByteArrayOutputStream()
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    val bitmapByte: ByteArray = baos.toByteArray()
                    val intent = Intent()
                    intent.putExtra("bitmap", bitmapByte)
                    intent.putExtra("title", viewModel.checkModel.value!!.sendModel.placeTitle)
                    setResult(200, intent)
                    locationSearchView.exitEditMode()
                    textureMapView.onDestroy()
                    GetLocationOperator.endOperate()
                    finish()
                }

                override fun onMapScreenShot(p0: Bitmap?, p1: Int) {}
            })
        }

    }

    override fun beforeCollapseStateChange(isCollapsing: Boolean) {
        if (isCollapsing) KeyBoardUtils.closeKeyboard(locationSearchView.windowToken, baseContext)
    }

    override fun onCollapseStateChange(isCollapsed: Boolean) {}

    override fun collapseStateChanged(isCollapsed: Boolean) {
        GetLocationOperator.getMap().uiSettings.isScaleControlsEnabled = !isCollapsed
        collapseButtonLayout.visibility = if (isCollapsed) VISIBLE else GONE
    }

    override fun onBackPressed() {
        if (locationSearchView.isEnterMode) {
            locationSearchView.exitEditMode()
            return
        }
        super.onBackPressed()
    }

}

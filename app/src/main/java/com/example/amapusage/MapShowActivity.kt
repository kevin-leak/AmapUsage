package com.example.amapusage

import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.AMap
import com.amap.api.maps.TextureMapView
import com.example.amapusage.collapse.ControlSensorPerformer
import com.example.amapusage.collapse.ScrollCollapseLayout
import com.example.amapusage.factory.GetLocationOperator
import com.example.amapusage.factory.IMapOperator
import com.example.amapusage.model.LocationViewModel
import com.example.amapusage.search.CheckModel
import com.example.amapusage.search.EntityCheckAdapter
import com.example.amapusage.search.EntityCheckSearch
import com.example.amapusage.utils.KeyBoardUtils
import com.example.amapusage.utils.ScreenUtils
import java.io.ByteArrayOutputStream


open class MapShowActivity : AppCompatActivity(), IMapOperator.LocationSourceLister,
    ControlSensorPerformer.CollapsingListener {
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
    private lateinit var sensor: ScrollCollapseLayout
    private lateinit var entityRecycleView: RecyclerView

    companion object {
        fun show(context: Context, cls: Class<out MapShowActivity>) {
            context.startActivity(Intent(context, cls))
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenUtils.setStatus(this)
        setContentView(R.layout.activity_show_map)
        initView(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)
        viewModel.getQueryText().observe(this, Observer<String> {
            if (!TextUtils.isEmpty(it)) GetLocationOperator.queryEntry(it)
            else progressBar.visibility = GONE
        })
        viewModel.checkModel.observe(this, Observer {
            if (it != null) changeSendButtonActive(true)
            else changeSendButtonActive(false)
        })
        viewModel.searchModelList.observe(this, Observer<MutableList<CheckModel>> {
            entityCheckAdapter.clearAddEntity(it)
        })
        viewModel.currentModelList.observe(this, Observer<MutableList<CheckModel>> {
            entityCheckAdapter.clearAddEntity(it)
        })
        GetLocationOperator.preWork(textureMapView, this)
            .bindCurrentButton(findViewById(R.id.current_location_button))
            .bindMapPin(findViewById(R.id.map_pin)).initData()
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

            override fun sourceCome(data: String) {
                super.sourceCome(data)
                viewModel.setQueryText(data)
            }

            override fun onSearchModeChange(isSearch: Boolean) {
                progressBar.visibility = if (isSearch) VISIBLE else GONE
                entityCheckAdapter.isSearch = isSearch
            }
        })
        textureMapView.map.setOnMapTouchListener { // collapsed下不可滑动
            if (sensor.isCollapsing) sensor.changeCollapseState(false)
        }

        entityRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!entityRecycleView.canScrollVertically(1)) {
                    Toast.makeText(baseContext, "dafa", Toast.LENGTH_SHORT).show()
                    entityCheckAdapter.addRefreshItem()
                }
            }
        })
    }

    private fun initAdapter() {
        entityRecycleView.layoutManager = LinearLayoutManager(this) //线性
        entityCheckAdapter = EntityCheckAdapter(this, viewModel)
        entityRecycleView.adapter = entityCheckAdapter
        entityCheckAdapter.listener = object : EntityCheckAdapter.CheckListenerImpl() {
            override fun checkByClick(model: CheckModel) {
                GetLocationOperator.moveToSelect(model.model.latLonPoint)
            }
        }
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
    override fun onResume() = super.onResume().also { textureMapView.onResume() }
    override fun onPause() = super.onPause().also { textureMapView.onPause() }
    fun outMap(v: View) = finish()
    override fun sourceCome(data: MutableList<CheckModel>, isMore: Boolean) {
        if (isMore) {
            viewModel.currentModelList.value!!.addAll(data)
            entityCheckAdapter.addMoreEntity(data)
        } else {
            if (entityCheckAdapter.isSearch) {
                viewModel.searchModelList.value = data
                progressBar.visibility = GONE
            } else {
                viewModel.currentModelList.value = data
            }
        }
    }

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
                    intent.putExtra("title", viewModel.checkModel.value!!.model.placeTitle)
                    setResult(200, intent)
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
        if (!isCollapsed) {
            entityRecycleView.scrollToPosition(entityCheckAdapter.checkPosition)
        }
        GetLocationOperator.getMap().uiSettings.isScaleControlsEnabled = !isCollapsed
        collapseButton.apply {
            visibility = if (isCollapsed) VISIBLE else GONE
            animation = AlphaAnimation(if (isCollapsed) 0f else 1f, if (isCollapsed) 1f else 0f)
            animation.duration = sensor.collapseDuration - 80
            animation.fillAfter = true
            animation.interpolator = AccelerateInterpolator()
            animation.start()
        }

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

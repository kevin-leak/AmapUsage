package com.example.amapusage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.amap.api.maps.TextureMapView
import com.example.amapusage.collapse.ControlSensorPerformer
import com.example.amapusage.collapse.ScrollCollapseLayout
import com.example.amapusage.factory.AMapOperator
import com.example.amapusage.factory.IMapOperator
import com.example.amapusage.model.LocationModel
import com.example.amapusage.model.LocationViewModel
import com.example.amapusage.search.EntityCheckAdapter
import com.example.amapusage.search.EntityCheckSearch
import com.example.amapusage.utils.KeyBoardUtils
import com.example.amapusage.utils.ScreenUtils
import kotlinx.android.synthetic.main.activity_show_map.*


class MapShowActivity : AppCompatActivity(), IMapOperator.LocationSourceLister,
    ControlSensorPerformer.CollapsingListener {
    private lateinit var viewModel: LocationViewModel
    private lateinit var sendLocationButton: Button
    private lateinit var collapseButton: ImageButton
    private lateinit var collapseLayout: RelativeLayout
    private lateinit var textureMapView: TextureMapView
    private lateinit var controllerLayout: LinearLayout
    private lateinit var locationSearchView: EntityCheckSearch
    private lateinit var sensor: ScrollCollapseLayout

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
        viewModel.getQueryText().observe(this, Observer<String> { AMapOperator.queryEntry(it) })
        viewModel.sendModel.observe(this, Observer<LocationModel?> {
            if (it != null) changeSendButtonActive(true)
            else changeSendButtonActive(false)
        })
        viewModel.searchModelList.observe(this, Observer<MutableList<LocationModel>> {})
        AMapOperator.preWork(textureMapView, this)
            .bindCurrentButton(findViewById(R.id.current_location_button))
            .bindMapPin(findViewById(R.id.map_pin))
        initAdapter()
        initListener()
    }

    private fun initView(savedInstanceState: Bundle?) {
        textureMapView = findViewById(R.id.texture_map_view)
        textureMapView.onCreate(savedInstanceState) // 此方法必须重写
        sensor = findViewById(R.id.scroll_collapse_sensor)
        collapseButton = findViewById(R.id.collapse_button)
        collapseLayout = findViewById(R.id.collapse_layout)
        controllerLayout = findViewById(R.id.controller_layout)
        locationSearchView = findViewById(R.id.ls_Search_view)
        sendLocationButton = findViewById(R.id.send_location_button)
        sensor.bindCollapsingView(textureMapView)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        sensor.setCollapsingListener(this)
        collapseLayout.setOnTouchListener { _, _ ->
            sensor.changeCollapseState(false)
            true
        }
        collapseButton.setOnTouchListener { _, _ ->
            sensor.changeCollapseState(false)
            true
        }
        locationSearchView.setSearchListener(object :
            EntityCheckSearch.OnSearchListenerIml() {
            override fun onEnterModeChange(isEnter: Boolean) {
                sensor.changeCollapseState(isEnter) // search输入与collapse连锁
            }
            override fun sourceCome(data: String) {
                super.sourceCome(data)
                viewModel.setQueryText(data)
            }
        })
        textureMapView.map.setOnMapTouchListener { // collapsed下不可滑动
            if (sensor.isCollapsing) sensor.changeCollapseState(false)
        }
    }

    private fun initAdapter() {
        rv.layoutManager = LinearLayoutManager(this) //线性
        val arrayList: ArrayList<String> = ArrayList()
        for (i in 0..49) arrayList.add("第" + i + "条数据")
        rv.adapter = EntityCheckAdapter(this, arrayList)
    }

    override fun onResume() = super.onResume().also { textureMapView.onResume() }

    override fun onPause() {
        super.onPause()
        textureMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        KeyBoardUtils.closeKeyboard(locationSearchView.windowToken, baseContext)
        textureMapView.onDestroy()
        AMapOperator.endOperate()
    }

    fun loadCurrentLocation(view: View) {
        AMapOperator.moveToCurrent()
        if (sensor.isCollapsing && locationSearchView.isEnterMode) sensor.changeCollapseState(false)
    }

    override fun moveCameraFinish() = changeSendButtonActive(true)
    override fun onMoveChange() = changeSendButtonActive(false)
    override fun sourceCome() {

    }

    private fun changeSendButtonActive(isClickable: Boolean) {
        sendLocationButton.isClickable = isClickable
        sendLocationButton.setTextColor(Color.parseColor(if (isClickable) "#ffffff" else "#808080"))
        val tmp = if (isClickable) R.drawable.shape_send_clickable
        else R.drawable.shape_send_unclikable
        sendLocationButton.background = resources.getDrawable(tmp)
    }

    fun onSendLocation(view: View) {
        Toast.makeText(this, "send location txt", Toast.LENGTH_LONG).show()
    }

    fun onBack(view: View) = finish()

    override fun beforeCollapseStateChange(isCollapsed: Boolean) {
        // 在发生扩展之前一定要关闭软键盘
        if (isCollapsed) KeyBoardUtils.closeKeyboard(locationSearchView.windowToken, baseContext)
    }

    override fun onCollapseStateChange(isCollapsed: Boolean) {}

    override fun collapseStateChanged(isCollapsed: Boolean) {
        AMapOperator.getMap().uiSettings.isScaleControlsEnabled = !isCollapsed
        collapseButton.apply {
            visibility = if (isCollapsed) VISIBLE else GONE
            animation = AlphaAnimation(if (isCollapsed) 0f else 1f, if (isCollapsed) 1f else 0f)
            animation.duration = 10
            animation.fillAfter = true
            animation.interpolator = AccelerateInterpolator(300f)
            animation.start()
        }
        controllerLayout.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                elevation = if (isCollapsed) 5f else 0f
            background = if (isCollapsed) null
            else resources.getDrawable(R.drawable.shape_controller_layout)
        }
    }

    override fun onBackPressed() {
        if (locationSearchView.isEnterMode) {
            locationSearchView.exitEditMode()
            return
        }
        super.onBackPressed()
    }
}

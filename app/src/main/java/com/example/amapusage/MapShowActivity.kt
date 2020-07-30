package com.example.amapusage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.LatLng
import com.example.amapusage.collapse.ControlSensorPerformer
import com.example.amapusage.collapse.ScrollCollapseLayout
import com.example.amapusage.factory.AMapOperator
import com.example.amapusage.factory.IMapOperator
import com.example.amapusage.search.EntityCheckSearch
import com.example.amapusage.search.EntityCheckAdapter
import com.example.amapusage.utils.KeyBoardUtils
import com.example.amapusage.utils.ScreenUtils
import kotlinx.android.synthetic.main.activity_show_map.*


class MapShowActivity : AppCompatActivity(), IMapOperator.LocationSourceLister {
    private lateinit var sendLocationButton: Button
    private lateinit var collapseButton: ImageButton
    private lateinit var collapseLayout: RelativeLayout
    private lateinit var textureMapView: TextureMapView
    private lateinit var controllerLayout: LinearLayout
    private lateinit var locationSearchView: EntityCheckSearch
    private lateinit var scrollCollapseSensor: ScrollCollapseLayout

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
        initMap(savedInstanceState)
        initAdapter()
        scrollCollapseSensor = findViewById(R.id.scroll_collapse_sensor)
        collapseButton = findViewById(R.id.collapse_button)
        collapseLayout = findViewById(R.id.collapse_layout)
        controllerLayout = findViewById(R.id.controller_layout)
        locationSearchView = findViewById(R.id.ls_Search_view)
        sendLocationButton = findViewById(R.id.send_location_button)
        scrollCollapseSensor.bindCollapsingView(textureMapView)
        linkageAnimation()
        locationSearchView.setSearchListener(object :
            EntityCheckSearch.OnSearchChangeListenerIml() {
            override fun onEnterModeChange(isEnter: Boolean) {
                scrollCollapseSensor.changeCollapseState(isEnter)
            }
        })
        textureMapView.map.setOnMapTouchListener {
            if (scrollCollapseSensor.isHeadCollapsing)
                scrollCollapseSensor.changeCollapseState(false)
        }
    }

    private fun linkageAnimation() {
        scrollCollapseSensor.setCollapsingListener(object :
            ScrollCollapseLayout.CollapsingListenerImpl() {
            override fun beforeCollapsingStateChange(sensor: ControlSensorPerformer.Sensor) {
                super.beforeCollapsingStateChange(sensor)
                // 在发生扩展之前一定要关闭软键盘
                if (sensor.isCollapsed()) { // 扩展
                    KeyBoardUtils.closeKeyboard(locationSearchView.windowToken, baseContext)
                }
            }

            override fun collapsingStateChanged(sensor: ControlSensorPerformer.Sensor) {
                collapseButtonAnimation(sensor.isCollapsed())
                if (sensor.isCollapsed()) {
                    controllerLayout.background = baseContext.resources
                        .getDrawable(R.drawable.shape_controller_layout)
                    controllerLayout.elevation = 5f
                    collapseButton.visibility = VISIBLE
                    AMapOperator.getMap().uiSettings.isScaleControlsEnabled = false
                } else {
                    controllerLayout.background = null
                    controllerLayout.elevation = 0f
                    collapseButton.visibility = GONE
                    AMapOperator.getMap().uiSettings.isScaleControlsEnabled = true
                }
            }
        })
        collapseLayout.setOnClickListener { scrollCollapseSensor.autoAnimation() }
        collapseButton.setOnClickListener { scrollCollapseSensor.autoAnimation() }
    }

    private fun initMap(savedInstanceState: Bundle?) {
        textureMapView = findViewById(R.id.texture_map_view)
        AMapOperator.prepareForWork(textureMapView.map, this)
            .bindCurrentButton(findViewById(R.id.current_location_button))
            .bindMapPin(findViewById(R.id.map_pin))
        textureMapView.onCreate(savedInstanceState) // 此方法必须重写
    }

    private fun initAdapter() {
        rv.layoutManager = LinearLayoutManager(this) //线性
        val arrayList: ArrayList<String> = ArrayList()
        for (i in 0..49) {
            arrayList.add("第" + i + "条数据")
        }
        rv.adapter = EntityCheckAdapter(this, arrayList)
    }

    fun collapseButtonAnimation(isShow: Boolean) {
        val deta = if (isShow) 1 else 0
        collapseButton.animation = AlphaAnimation(deta.and(0).toFloat(), deta.and(1).toFloat())
        collapseButton.animation.apply {
            duration = 10
            fillAfter = true
            interpolator = AccelerateInterpolator(300f)
        }.start()
    }

    override fun onResume() {
        super.onResume()
        textureMapView.onResume()
    }

    override fun onPause() {
        KeyBoardUtils.closeKeyboard(locationSearchView.windowToken, baseContext)
        super.onPause()
        textureMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        textureMapView.onDestroy()
        AMapOperator.endOperate()
    }

    fun loadCurrentLocation(view: View) {
        if (scrollCollapseSensor.isHeadCollapsing && locationSearchView.isEnterEdit)
            scrollCollapseSensor.changeCollapseState(false)
        AMapOperator.moveToCurrent()
    }

    override fun moveCameraFinish() {
        changeSendButtonActive(true)
    }

    override fun onMoveChange() {
        changeSendButtonActive(false)
    }

    private fun changeSendButtonActive(isClickAble: Boolean) {
        if (isClickAble) sendLocationButton.apply {
            isClickable = true
            background = getDrawable(R.drawable.shape_send_button)
            setTextColor(Color.parseColor("#ffffff"))
        }
        else sendLocationButton.apply {
            isClickable = false
            background = getDrawable(R.drawable.shape_send_button_gray)
            setTextColor(Color.parseColor("#808080"))
        }
    }

    fun onSendLocation(view: View) {
        Toast.makeText(this, "send location txt", Toast.LENGTH_LONG).show()
    }

    fun onBack(view: View) {
        finish()
    }
}

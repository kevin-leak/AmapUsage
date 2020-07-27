package com.e

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.MyLocationStyle
import com.example.amapusage.IMapClient
import com.example.amapusage.utils.KeyBoardUtils
import com.example.amapusage.LocationAdapter
import com.example.amapusage.R
import com.example.amapusage.collapse.ControlSensorPerformer
import com.example.amapusage.collapse.ScrollCollapseLayout
import kotlinx.android.synthetic.main.activity_show_map.*


class MapShowActivity : AppCompatActivity(), IMapClient.InfoArrivals {
    private lateinit var btnACK: TextView
    private lateinit var collapseButton: ImageButton
    private lateinit var scrollCollapseController: ScrollCollapseLayout
    private lateinit var mapView: TextureMapView
    private lateinit var map: AMap

    private lateinit var searchView: SearchView

    companion object {
        fun show(context: Context, cls: Class<out MapShowActivity>) {
            context.startActivity(Intent(context, cls))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_map)
        initMap(savedInstanceState)
        initAdapter()

        scrollCollapseController = findViewById(R.id.scroll_collapse_controller)
        collapseButton = findViewById(R.id.collapse_button)
        btnACK = findViewById(R.id.btn_ack)
        searchView = findViewById(R.id.sv_Search)
        searchView.findViewById<ImageView>(R.id.search_close_btn).setImageDrawable(null)
        searchView.findViewById<View>(R.id.search_plate).background = null
        searchView.findViewById<View>(R.id.submit_area).background = null
        linkageAnimation()
    }

    private fun linkageAnimation() {
        scrollCollapseController.setCollapsingListener(object :
            ScrollCollapseLayout.CollapsingListenerImpl() {
            override fun beforeCollapsingStateChange(sensor: ControlSensorPerformer.Sensor) {
                super.beforeCollapsingStateChange(sensor)
                // fixme 慢慢显示
                collapseButtonAnimation(!sensor.isCollapsing())
            }
            override fun collapsingStateChanged(sensor: ControlSensorPerformer.Sensor) {
                super.collapsingStateChanged(sensor)
                if (!sensor.isCollapsing()) KeyBoardUtils.closeKeyboard(searchView, baseContext)
                if (!sensor.isCollapsing()) searchView.clearFocus()
            }
        })
        findViewById<RelativeLayout>(R.id.rl_search)
            .setOnClickListener { scrollCollapseController.animation() }
        collapseButton.setOnClickListener { scrollCollapseController.animation() }
        btnACK.setOnClickListener {
            scrollCollapseController.animation()
            if (!scrollCollapseController.isHeadCollapsing) {
                searchView.clearFocus()
                KeyBoardUtils.closeKeyboard(searchView, baseContext)
            }
        }
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollCollapseController.expand()
            btnACK.visibility = when (hasFocus) {
                false -> GONE
                true -> VISIBLE
            }
        }
    }

    private fun initMap(savedInstanceState: Bundle?) {
        IMapClient.getLocation(this) // 初始化，获取当前位置的数据.
        mapView = findViewById(R.id.map)
        map = mapView.map
        IMapClient.getLocation(this) // 注册获取当前位置信息.
        // 蓝点设置
        map.myLocationStyle = MyLocationStyle().apply {
            interval(2000) //连续定位模式下的刷新间隔
            strokeColor(Color.argb(0, 0, 0, 0))
            radiusFillColor(Color.argb(0, 0, 0, 0))
            myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE) // 随着定位而移动
            showMyLocation(true)// 蓝点是否显示，5.1.0版本后支持
        }
        map.apply {
            moveCamera(CameraUpdateFactory.zoomTo(10f)) // 当前位置缩放
            uiSettings.isMyLocationButtonEnabled = true // 是否显示定位锚点图标
            isMyLocationEnabled = true                  // 是否显示蓝点
            uiSettings.isZoomControlsEnabled = false    // 是否显示缩放按钮
            uiSettings.logoPosition= AMapOptions.LOGO_POSITION_BOTTOM_RIGHT // 设置地图logo显示在右下方
        }

        mapView.onCreate(savedInstanceState) // 此方法必须重写
    }

    private fun initAdapter() {
        rv.layoutManager = LinearLayoutManager(this) //线性
        val arrayList: ArrayList<String> = ArrayList()
        for (i in 0..49) {
            arrayList.add("第" + i + "条数据")
        }
        rv
        rv.adapter = LocationAdapter(this, arrayList)
    }

    fun collapseButtonAnimation(isShow: Boolean) {
        var start = 0f
        var end = 0f
        if (isShow) {
            end = 1f
            collapseButton.visibility = VISIBLE
        } else {
            start = 1f
            collapseButton.visibility = GONE
        }
        val animation = AlphaAnimation(start, end)
        animation.duration = 300
        animation.fillAfter = true
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                collapseButton.clearAnimation()
            }
        })
        collapseButton.animation = animation
        animation.start()
    }


    override fun arrival(location: AMapLocation) {

    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }


}

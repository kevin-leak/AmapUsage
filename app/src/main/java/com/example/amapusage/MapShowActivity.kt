package com.example.amapusage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.CameraPosition
import com.example.amapusage.IMapClient
import com.example.amapusage.utils.KeyBoardUtils
import com.example.amapusage.LocationAdapter
import com.example.amapusage.AMapConfiguration
import com.example.amapusage.R
import com.example.amapusage.collapse.ControlSensorPerformer
import com.example.amapusage.collapse.ScrollCollapseLayout
import com.example.amapusage.utils.ScreenUtils
import kotlinx.android.synthetic.main.activity_show_map.*


class MapShowActivity : AppCompatActivity(), IMapClient.InfoArrivals {
    private lateinit var btnACK: TextView
    private lateinit var collapseButton: ImageButton
    private lateinit var scrollCollapseSensor: ScrollCollapseLayout
    private lateinit var textureMapView: TextureMapView
    private lateinit var searchView: SearchView
    private lateinit var controllerLayout: RelativeLayout

    companion object {
        fun show(context: Context, cls: Class<out MapShowActivity>) {
            context.startActivity(Intent(context, cls))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenUtils.setStatus(this)
        setContentView(R.layout.activity_show_map)
        initMap(savedInstanceState)
        initAdapter()

        scrollCollapseSensor = findViewById(R.id.scroll_collapse_sensor)
        collapseButton = findViewById(R.id.collapse_button)
        btnACK = findViewById(R.id.btn_ack)
        searchView = findViewById(R.id.sv_Search)
        searchView.findViewById<ImageView>(R.id.search_close_btn).setImageDrawable(null)
        searchView.findViewById<View>(R.id.search_plate).background = null
        searchView.findViewById<View>(R.id.submit_area).background = null
        controllerLayout = findViewById(R.id.controller_layout)
        val sendLocation = findViewById<Button>(R.id.send_location)
        linkageAnimation()

        textureMapView.map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChangeFinish(p0: CameraPosition?) {
                sendLocation.background =
                    baseContext.resources.getDrawable(R.drawable.shape_send_botton)
            }

            override fun onCameraChange(p0: CameraPosition?) {
                if (sendLocation.background != baseContext.resources.getDrawable(R.drawable.shape_send_botton_gray))
                    sendLocation.background =
                        baseContext.resources.getDrawable(R.drawable.shape_send_botton_gray)
            }


        })
    }

    private fun linkageAnimation() {
        scrollCollapseSensor.setCollapsingListener(object :
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
                if (sensor.isCollapsing()) {
                    controllerLayout.background = baseContext.resources
                        .getDrawable(R.drawable.shape_controller_layout)
                    controllerLayout.elevation = 5f
                } else {
                    controllerLayout.background = null
                    controllerLayout.elevation = 0f
                }
            }
        })
        findViewById<RelativeLayout>(R.id.rl_search)
            .setOnClickListener { scrollCollapseSensor.animation() }

        collapseButton.setOnClickListener { scrollCollapseSensor.animation() }

        btnACK.setOnClickListener {
            scrollCollapseSensor.animation()
            if (!scrollCollapseSensor.isHeadCollapsing) {
                searchView.clearFocus()
                KeyBoardUtils.closeKeyboard(searchView, baseContext)
            }
        }

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollCollapseSensor.collapsing()
            btnACK.visibility = when (hasFocus) {
                false -> GONE
                true -> VISIBLE
            }
        }

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!scrollCollapseSensor.isHeadCollapsing) {
                    scrollCollapseSensor.expand()
                    recyclerView.stopScroll()
                    recyclerView.stopNestedScroll()

                }
            }
        })
    }

    private fun initMap(savedInstanceState: Bundle?) {
        IMapClient.getLocation(this) // 初始化，获取当前位置的数据.
        textureMapView = findViewById(R.id.texture_map_view)
        AMapConfiguration.buildUIConfig(textureMapView.map)
        textureMapView.onCreate(savedInstanceState) // 此方法必须重写
    }

    private fun initAdapter() {
        rv.layoutManager = LinearLayoutManager(this) //线性
        val arrayList: ArrayList<String> = ArrayList()
        for (i in 0..49) {
            arrayList.add("第" + i + "条数据")
        }
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
        textureMapView.onResume()
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        textureMapView.onPause()
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        textureMapView.onDestroy()
    }


}

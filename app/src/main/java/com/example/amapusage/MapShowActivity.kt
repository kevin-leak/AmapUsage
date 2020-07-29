package com.example.amapusage

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.location.AMapLocation
import com.amap.api.maps.AMap
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.CameraPosition
import com.example.amapusage.collapse.ControlSensorPerformer
import com.example.amapusage.collapse.ControlSensorPerformer.Companion.BEFORE_COLLLAPSING
import com.example.amapusage.collapse.ScrollCollapseLayout
import com.example.amapusage.search.IHintSearchView
import com.example.amapusage.search.HintSearchView
import com.example.amapusage.utils.KeyBoardUtils
import com.example.amapusage.utils.ScreenUtils
import kotlinx.android.synthetic.main.activity_show_map.*


class MapShowActivity : AppCompatActivity(), IMapClient.InfoArrivals {
    private val TAG = "MapShowActivity"
    private var sendGrayDrawable: Drawable? =
        App.getAppContext().resources.getDrawable(R.drawable.shape_send_botton_gray)
    private var sendDrawable: Drawable? =
        App.getAppContext().resources.getDrawable(R.drawable.shape_send_button)
    private lateinit var sendLocationButton: Button
    private lateinit var collapseButton: ImageButton
    private lateinit var collapseLayout: RelativeLayout
    private lateinit var textureMapView: TextureMapView
    private lateinit var controllerLayout: LinearLayout
    private lateinit var lsSearchView: HintSearchView
    private lateinit var scrollCollapseSensor: ScrollCollapseLayout

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
        collapseLayout = findViewById(R.id.collapse_layout)
        controllerLayout = findViewById(R.id.controller_layout)
        lsSearchView = findViewById(R.id.ls_Search_view)
        sendLocationButton = findViewById(R.id.send_location_button)

        linkageAnimation()

        textureMapView.map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChangeFinish(p0: CameraPosition?) {
                sendLocationButton.background = sendDrawable
            }

            override fun onCameraChange(p0: CameraPosition?) {
                if (sendLocationButton.background != sendGrayDrawable)
                    sendLocationButton.background = sendGrayDrawable
                Log.e(TAG, "onCameraChange: ${p0.toString()}")
            }
        })
        lsSearchView.setSearchListener(object : HintSearchView.OnSearchChangeListenerIml() {
            override fun onEnterModeChange(isEnter: Boolean) {
                when (isEnter) {
                    true -> scrollCollapseSensor.collapsing()
                    false -> scrollCollapseSensor.expand()
                }
            }
        })
    }

    private fun linkageAnimation() {
        scrollCollapseSensor.setCollapsingListener(object :
            ScrollCollapseLayout.CollapsingListenerImpl() {
            override fun beforeCollapsingStateChange(sensor: ControlSensorPerformer.Sensor) {
                super.beforeCollapsingStateChange(sensor)
                collapseButtonAnimation(!sensor.isCollapsed())
                // 在发生扩展之前一定要关闭软键盘
                if (sensor.isCollapsed()) KeyBoardUtils.closeKeyboard(
                    lsSearchView.getEditView(),
                    baseContext
                )
            }

            override fun collapsingStateChanged(sensor: ControlSensorPerformer.Sensor) {
                if (sensor.isCollapsed()) {
                    controllerLayout.background = baseContext.resources
                        .getDrawable(R.drawable.shape_controller_layout)
                    controllerLayout.elevation = 5f
                    collapseButton.visibility = VISIBLE
                } else {
                    controllerLayout.background = null
                    controllerLayout.elevation = 0f
                    collapseButton.visibility = GONE
                }
            }
        })
        collapseLayout.setOnClickListener { scrollCollapseSensor.animation() }
        collapseButton.setOnClickListener { scrollCollapseSensor.animation() }
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
        if (isShow) end = 1f
        else start = 1f
        val animation = AlphaAnimation(start, end)
        animation.duration = 10
        animation.fillAfter = true
        animation.interpolator = AccelerateInterpolator(300f)
        collapseButton.animation = animation
        animation.start()
    }


    override fun arrival(location: AMapLocation) {

    }

    override fun onResume() {
        super.onResume()
        textureMapView.onResume()
    }

    override fun onPause() {
        // 当pause的时候要关闭软键盘
        KeyBoardUtils.closeKeyboard(lsSearchView.getEditView(), baseContext)
        super.onPause()
        textureMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        textureMapView.onDestroy()
    }
}

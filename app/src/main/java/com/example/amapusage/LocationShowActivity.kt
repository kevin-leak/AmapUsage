package com.example.amapusage

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.AMap
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
    val TAG = "MapShowActivity"
    private lateinit var entityCheckAdapter: EntityCheckAdapter
    private lateinit var viewModel: LocationViewModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenUtils.setStatus(this)
        setContentView(R.layout.activity_show_location)
        textureMapView.onCreate(savedInstanceState) // 此方法必须重写
        sensor.bindCollapsingView(textureMapView)
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
            .bindCurrentButton(currentLocationButton)
        GetLocationOperator.bindModel(viewModel)
        initAdapter()
        initListener()
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
                progressBar.visibility = GONE
                if (isSearch) {
                    viewModel.searchModelList.value = mutableListOf()
                    viewModel.tmp = viewModel.checkModel.value
                    entityCheckAdapter.switchData(viewModel.searchModelList)
                } else {
                    viewModel.checkModel.value = viewModel.tmp
                    viewModel.tmp = null
                    if (viewModel.checkModel.value != null)
                        GetLocationOperator.moveToSelect(viewModel.checkModel.value!!.lonPoint)
                    viewModel.searchModelList.value = mutableListOf()
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
                entityRecycleView.post { GetLocationOperator.moveToSelect(viewModel.checkModel.value!!.lonPoint) }
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
        if (GetLocationOperator.lock) sensor.setLock()
        progressBar.visibility = VISIBLE
        viewModel.checkModel.value = null
        viewModel.searchModelList.value = mutableListOf()
        viewModel.currentModelList.value = mutableListOf()
    }

    override fun loadDataDone() {
        if (!GetLocationOperator.lock) sensor.unLock()
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
                override fun onMapScreenShot(bitmap: Bitmap) {
                    val bit = BitmapUtils.createScaledBitmap(bitmap, 100, 50)
                    val baos = ByteArrayOutputStream()
                    bit.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    val bitmapByte: ByteArray = baos.toByteArray()
                    baos.close()
                    val intent = Intent()
                    intent.putExtra("bitmap", bitmapByte)
                    intent.putExtra("sendModel", viewModel.checkModel.value!!.sendModel)
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
        GetLocationOperator.clearMapPin()
    }

    override fun onCollapseStateChange(isCollapsed: Boolean) {
        GetLocationOperator.resetCenterMark()
    }

    override fun collapseStateChanged(isCollapsed: Boolean) {
        GetLocationOperator.getMap().uiSettings.isScaleControlsEnabled = !isCollapsed
        collapseButtonLayout.apply {
            visibility = if (isCollapsed) VISIBLE else GONE
//            animation = AlphaAnimation(if (isCollapsed) 0f else 1f, if (isCollapsed) 1f else 0f)
//            animation.duration = 10
//            animation.fillAfter = true
//            animation.interpolator = AccelerateInterpolator()
//            animation.start()
        }
        GetLocationOperator.resetCenterMark()
    }

    override fun onBackPressed() {
        if (locationSearchView.isEnterMode) {
            locationSearchView.exitEditMode()
            return
        }
        super.onBackPressed()
    }
}

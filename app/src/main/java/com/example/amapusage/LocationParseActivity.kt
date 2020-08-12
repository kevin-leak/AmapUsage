package com.example.amapusage

//import com.example.amapusage.operator.ParseLocationOperator
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.model.LatLng
import com.example.amapusage.factory.IMapOperator
import com.example.amapusage.factory.ParseLocationOperator
import com.example.amapusage.model.LocationModel
import com.example.amapusage.utils.ScreenUtils
import kotlinx.android.synthetic.main.activity_parse_location.*
import java.lang.reflect.Method


class LocationParseActivity : AppCompatActivity(), IMapOperator.LocationSourceLister {
    private lateinit var operator: ParseLocationOperator
    private lateinit var locationModel: LocationModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenUtils.setWhiteStatusBlackFont(this)
        setContentView(R.layout.activity_parse_location)
        textureMapView.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }


        operator = ParseLocationOperator()
        operator.preWork(textureMapView, this)
            .bindCurrentButton(currentLocationButton)
            .isNeedCenterPin = false
        locationModel = intent.getParcelableExtra(MainActivity.RESULT_SEND_MODEL) as LocationModel
        tvTitle.text = locationModel.placeTitle
        tvDetails.text = locationModel.placeDesc
        operator.addPositionMarker(LatLng(locationModel.latitude, locationModel.longitude))
        operator.moveToCurrent()
    }


//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_location_info, menu)
//        return true
//    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        menuInflater.inflate(R.menu.menu_location_info, menu)
    }

    override fun moveCameraFinish() {

    }

    override fun onMoveChange() {

    }

    override fun startLoadNewData() {

    }

    override fun loadDataDone() {

    }

    fun loadCurrentLocation(view: View) = operator.moveToCurrent()
    fun onMapForward(view: View) {}

    override fun onDestroy() {
        operator.endOperate()
        super.onDestroy()
    }
}
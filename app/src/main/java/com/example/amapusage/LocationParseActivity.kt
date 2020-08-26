package com.example.amapusage

//import com.example.amapusage.operator.ParseLocationOperator
import android.os.Build
import android.os.Bundle
import android.view.ContextMenu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.amap.api.maps.model.LatLng
import com.example.amapusage.factory.IMapOperator
import com.example.amapusage.factory.ParseLocationOperator
import com.example.amapusage.model.LocationModel
import com.example.amapusage.sheet.BottomSheetFragment
import com.example.amapusage.sheet.SheetAction
import com.example.amapusage.sheet.maps.MapActionBuilder
import com.example.amapusage.sheet.maps.MapFocusHolder
import com.example.amapusage.sheet.maps.MapSheetAction
import com.example.amapusage.utils.ScreenUtils
import kotlinx.android.synthetic.main.activity_parse_location.*


class LocationParseActivity : AppCompatActivity(), IMapOperator.LocationSourceLister,
    IMapOperator.LocateCurrentState {
    private lateinit var operator: ParseLocationOperator
    private lateinit var locationModel: LocationModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenUtils.setWhiteStatusBlackFont(this)
        setContentView(R.layout.activity_parse_location)
        textureMapView.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        navigation.setOnClickListener { finish() }

        operator = ParseLocationOperator()
        operator.preWork(textureMapView, this)
            .bindCurrentButton(currentLocationButton, this)
            .isNeedCenterPin = false
        locationModel = intent.getParcelableExtra(MainActivity.RESULT_SEND_MODEL) as LocationModel
        tvTitle.text = locationModel.placeTitle
        tvDetails.text = locationModel.placeDesc
        operator.addPositionMarker(LatLng(locationModel.latitude, locationModel.longitude))
        operator.moveTo(locationModel.latitude, locationModel.longitude)
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

    fun onMapForward(view: View) {
        val bottomDialogSheet = BottomSheetFragment(supportFragmentManager)
        val availableMaps: MutableList<MapSheetAction> = MapActionBuilder()
            .getAvailableMaps(locationModel.latitude, locationModel.longitude)
        bottomDialogSheet.setFocusSheet(
            MapFocusHolder(
                this.layoutInflater.inflate(R.layout.item_bottom_sheet_focus, null)
            )
        )
        bottomDialogSheet.setVisibleTitle(View.GONE)
        bottomDialogSheet.setSheets(availableMaps).show()
    }

    override fun onDestroy() {
        operator.endOperate()
        super.onDestroy()
    }

    override fun performLocateCurrent(b: Boolean): Boolean {
        operator.moveToCurrent()
        return false
    }

    fun showMenu(view: View) {
        val bottomDialogSheet = BottomSheetFragment(supportFragmentManager)
        val shareChat = object : SheetAction {
            override val description: String = "Amap Usage"
        }
        val addFavorites = object : SheetAction {
            override val description: String = "Add to Favorite"
        }
        val sheets: MutableList<SheetAction> = mutableListOf()
        sheets.add(shareChat)
        sheets.add(addFavorites)
        bottomDialogSheet.setVisibleTitle(View.GONE)
        bottomDialogSheet.setSheets(sheets).show()
    }

}
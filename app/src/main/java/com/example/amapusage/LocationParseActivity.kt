package com.example.amapusage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.amapusage.factory.IMapOperator
import com.example.amapusage.factory.ParseLocationOperator
//import com.example.amapusage.operator.ParseLocationOperator
import com.example.amapusage.utils.ScreenUtils
import kotlinx.android.synthetic.main.activity_parse_location.*

class LocationParseActivity : AppCompatActivity(), IMapOperator.LocationSourceLister {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenUtils.setStatus(this)
        setContentView(R.layout.activity_parse_location)
        textureMapView.onCreate(savedInstanceState)
        ParseLocationOperator.preWork(textureMapView, this)
    }

    override fun moveCameraFinish() {

    }

    override fun onMoveChange() {

    }

    override fun startLoadNewData() {

    }

    override fun loadDataDone() {

    }

    fun loadCurrentLocation(view: View) = ParseLocationOperator.moveToCurrent()
}
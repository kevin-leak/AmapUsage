package com.example.amapusage

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.amapusage.model.LocationModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_message.*


class MainActivity : AppCompatActivity() {
    private lateinit var locationModel: LocationModel

    companion object {
        const val TAG = "kyle-map-MapShow"
        const val RESULT_CODE_SEND_MODEL = 200
        const val RESULT_BITMAP = "bitmap"
        const val RESULT_SEND_MODEL = "RESULT_SEND_MODEL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun getLocation(view: View) {
        startActivityForResult(Intent(this, LocationShowActivity::class.java), 1);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            val bis = data.getByteArrayExtra(RESULT_BITMAP)
            val bitmap = BitmapFactory.decodeByteArray(bis, 0, bis!!.size)
            locationModel =
                data.getParcelableExtra(RESULT_SEND_MODEL) as LocationModel
            cdvMessageItem.visibility = View.VISIBLE
            ivMap.setImageBitmap(bitmap)
            tvTitle.text = locationModel.placeTitle
            tvDetails.text = locationModel.placeDesc
        }
    }

    fun parseLocation(view: View) = Intent().run {
        putExtra(RESULT_SEND_MODEL, locationModel)
        setClass(this@MainActivity, LocationParseActivity::class.java)
        startActivity(this)
    }

}
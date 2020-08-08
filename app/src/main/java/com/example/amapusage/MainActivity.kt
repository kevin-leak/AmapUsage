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
            val bis = data.getByteArrayExtra(LocationShowActivity.RESULT_BITMAP)
            val bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.size)
            val locationModel = data.getParcelableExtra(LocationShowActivity.RESULT_SEND_MODEL) as LocationModel
            cdvMessageItem.visibility = View.VISIBLE
            ivMap.setImageBitmap(bitmap)
            tvTitle.text = locationModel.placeTitle
            tvDetails.text = locationModel.placeDesc
        }
    }

    fun parseLocation(view: View) = startActivity(Intent(this, LocationParseActivity::class.java));

}
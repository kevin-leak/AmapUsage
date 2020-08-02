package com.example.amapusage

import android.content.Intent
import android.graphics.BitmapFactory
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun getLocation(view: View) {
        startActivityForResult(Intent(this, MapShowActivity::class.java), 1);
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null){
            val bis = data.getByteArrayExtra("bitmap")
            val bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.size)
            val tvTitle = data.getStringExtra("title")
            findViewById<CardView>(R.id.cdv).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.iv).setImageBitmap(bitmap)
            findViewById<TextView>(R.id.tv_title).text = tvTitle
        }
    }
}
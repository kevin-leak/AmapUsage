package com.example.amapusage

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.example.amapusage.model.LocationModel
import com.example.amapusage.utils.DialogUtils
import kotlinx.android.synthetic.main.item_message.*
import pub.devrel.easypermissions.EasyPermissions


open class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var locationModel: LocationModel

    companion object {
        const val TAG = "kyle-map-MapShow"
        const val RESULT_CODE_SEND_MODEL = 200
        const val RESULT_BITMAP = "bitmap"
        const val RESULT_SEND_MODEL = "RESULT_SEND_MODEL"
    }

    private var needPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun getLocation(view: View) {
        ActivityCompat.requestPermissions(this, needPermissions, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return
        val bis = data.getByteArrayExtra(RESULT_BITMAP)
        val bitmap = BitmapFactory.decodeByteArray(bis, 0, bis!!.size)
        locationModel =
            data.getParcelableExtra(RESULT_SEND_MODEL) as LocationModel
        cdvMessageItem.visibility = View.VISIBLE
        ivMap.setImageBitmap(bitmap)
        tvTitle.text = locationModel.placeTitle
        tvDetails.text = locationModel.placeDesc
    }

    fun parseLocation(view: View) = Intent().run {
        putExtra(RESULT_SEND_MODEL, locationModel)
        setClass(this@MainActivity, LocationParseActivity::class.java)
        startActivity(this)
    }

    override fun onRequestPermissionsResult(rCode: Int, perms: Array<out String>, gRlt: IntArray) {
        super.onRequestPermissionsResult(rCode, perms, gRlt)
        EasyPermissions.onRequestPermissionsResult(rCode, perms, gRlt, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                DialogUtils.gpsPermissionDialog(this@MainActivity)
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            startActivityForResult(Intent(this, LocationShowActivity::class.java), 1)
        }
    }

}
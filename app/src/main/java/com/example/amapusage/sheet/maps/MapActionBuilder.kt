package com.example.amapusage.sheet.maps

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import com.example.amapusage.App
import com.example.amapusage.R
import java.io.File


class MapActionBuilder {

    companion object {
        const val AMAP = "com.autonavi.minimap"
        const val BAIDU_MAP = "com.baidu.BaiduMap"
        const val TENCENT_MAP = "com.tencent.map"
        const val GOOGLE_MAP = "com.google.android.apps.maps"
        const val DIDI_TAXI = "com.sdu.didi.psnger"
    }

    private val pkgSet: MutableSet<MapSheetAction> = mutableSetOf()

    init {
        pkgSet.add(MapSheetAction(TENCENT_MAP, "Tencent Maps").apply { isMust = true })
        pkgSet.add(MapSheetAction(GOOGLE_MAP, "Maps"))
        pkgSet.add(MapSheetAction(BAIDU_MAP, "Baidu Map"))
        pkgSet.add(MapSheetAction(AMAP, "AMap"))
        pkgSet.add(TaxiSheetAction(DIDI_TAXI, "DiDi Travel").apply {
            title = "Use the DiDi Program to Get a Ride"
            icon = R.drawable.ic_didi
            isMust = true
        })
    }

    fun addSheet(sheet: MapSheetAction) {
        pkgSet.add(sheet)
    }

    fun getAvailableMaps(latitude: Double, longitude: Double): MutableList<MapSheetAction> {
        val sheets: MutableList<MapSheetAction> = mutableListOf()
        pkgSet.forEach {
            if (isMapAppAvailable(it.packageName) || it.isMust) {
                it.latitude = latitude
                it.longitude = longitude
                sheets.add(it)
            }
        }
        return sheets
    }

    private fun isMapAppAvailable(mapPackageName: String): Boolean {
        var packageInfo: PackageInfo?
        try {
            packageInfo = App.appContext.packageManager.getPackageInfo(mapPackageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            packageInfo = null
            e.printStackTrace()
        }
        return packageInfo != null
    }

    private fun isInstallByread(packageName: String): Boolean {
        return File(Environment.getExternalStorageDirectory().path + packageName).exists()
    }

    private fun haveDirect(): Boolean {
        val uri = Uri.parse("geo:24.473306,118.123456");
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val componentName = intent.resolveActivity(App.appContext.packageManager)
        return componentName != null
    }

}
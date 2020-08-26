package com.example.amapusage.sheet.maps

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import com.example.amapusage.App
import com.example.amapusage.sheet.SheetAction
import kotlin.properties.Delegates

open class MapSheetAction(val packageName: String, override var description: String) : SheetAction {

    var isMust: Boolean = false
    var latitude by Delegates.notNull<Double>()
    var longitude by Delegates.notNull<Double>()

    override fun commonAction() {
        if (!isMapAppAvailable()) {
            downloadMapApp()
            return
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage(packageName)
            intent.data = Uri.parse("geo:${latitude},${longitude}")
            App.appContext.startActivity(intent)
        }
    }

    private fun downloadMapApp() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        App.appContext.startActivity(intent)
    }

    private fun isMapAppAvailable(): Boolean {
        var packageInfo: PackageInfo?
        try {
            packageInfo = App.appContext.packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            packageInfo = null
            e.printStackTrace()
        }
        return packageInfo != null
    }


}
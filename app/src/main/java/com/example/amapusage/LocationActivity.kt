package com.example.amapusage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Secure
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amapusage.utils.DialogUtils
import com.example.amapusage.utils.NetworkUtil
import com.example.amapusage.utils.ScreenUtils


abstract class LocationActivity : AppCompatActivity() {

    private lateinit var netStateMonitor: ConnectionStateMonitor
    private var gspStateReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusStyle()
        setContentView(getResourcesId())
        initView(savedInstanceState)
        initData()
        registerListener()
    }

    open fun registerListener() {
        dealGPSProblem()
        dealNetProblem()
    }

    open fun initData() {}
    open fun initView(savedInstanceState: Bundle?) {}
    private fun setStatusStyle() {
        ScreenUtils.setStatus(this)
    }

    abstract fun getResourcesId(): Int
    open fun netStateChange(hasNetwork: Boolean) {}
    open fun gpsStateChange(haveGspPermission: Boolean) {}
    override fun onDestroy() {
        contentResolver.unregisterContentObserver(mGpsMonitor)
//        if (gspStateReceiver != null) unregisterReceiver(gspStateReceiver)
//        netStateMonitor.end()
        unregisterReceiver(netReceiver)
        super.onDestroy()
    }

    private fun dealGPSProblem() {
        if (!haveGspPermission()) {
            DialogUtils.gpsNotifyDialog(this)
            initGspReceiver()
            contentResolver.registerContentObserver(
                Secure.getUriFor(Settings.System.LOCATION_PROVIDERS_ALLOWED),
                false, mGpsMonitor
            )
        }
    }

    private val mGpsMonitor: ContentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            val enabled: Boolean = (getSystemService(LOCATION_SERVICE) as LocationManager)
                .isProviderEnabled(LocationManager.GPS_PROVIDER)
            gpsStateChange(enabled)
        }
    }

    private fun initGspReceiver() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_LOCALE_CHANGED)
        gspStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                if (Intent.ACTION_PROVIDER_CHANGED != intent.action) return
                gpsStateChange(haveGspPermission())
            }
        }
        registerReceiver(gspStateReceiver, filter)
    }

    private fun haveGspPermission(): Boolean {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    private fun dealNetProblem() {
//        netStateMonitor = ConnectionStateMonitor(this)
//        netStateMonitor.start()

        if (!NetworkUtil.hasNetwork()) {
            Toast.makeText(this, R.string.no_intent, Toast.LENGTH_LONG).show()
        }
        val filter = IntentFilter()
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(netReceiver, filter)
    }

    private val netReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // 如果相等的话就说明网络状态发生了变化
            // 如果相等的话就说明网络状态发生了变化
            if (intent!!.action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                val netWorkState = NetworkUtil.hasNetwork()
                // 接口回调传过去状态的类型
                netStateChange(netWorkState)
            }
        }
    }

    inner class ConnectionStateMonitor(val context: Context) : NetworkCallback() {
        private lateinit var connectivityManager: ConnectivityManager
        private val networkRequest: NetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        fun start() {
            connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.registerNetworkCallback(networkRequest, this)
        }

        override fun onAvailable(network: Network) = netStateChange(true)

        override fun onLost(network: Network) {
            Toast.makeText(context, R.string.no_intent, Toast.LENGTH_LONG).show()
            netStateChange(false)
        }

        fun end() = connectivityManager.unregisterNetworkCallback(this)
    }

}
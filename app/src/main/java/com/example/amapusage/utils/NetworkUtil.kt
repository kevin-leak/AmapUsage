package com.example.amapusage.utils

import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import android.util.Log

object NetworkUtil {
    private const val TAG = "NetworkUtil"
    fun hasNetwork(context: Context): Boolean {
        val status = getNetworkStatus(context)
        return status != XNetworkStatus.NOTREACHABLE && status != XNetworkStatus.UNKNOWN
    }

    private fun getNetworkStatus(context: Context): XNetworkStatus {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        if (activeNetwork != null) {
            if (activeNetwork.isConnectedOrConnecting) {
                return when (activeNetwork.type) {
                    ConnectivityManager.TYPE_WIFI -> XNetworkStatus.REACHABLEVIAWIFI
                    else -> getMobileNetWorkClass(context)
                }
            } else {
                Log.d(TAG, "getNetworkStatus: network state is " + activeNetwork.state)
            }
        } else {
            Log.d(TAG, "getNetworkStatus: no active network.")
        }
        return XNetworkStatus.NOTREACHABLE
    }

    private fun getMobileNetWorkClass(context: Context): XNetworkStatus {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return when (telephonyManager.networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN
            -> XNetworkStatus.REACHABLEVIAWWAN2G

            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP
            -> XNetworkStatus.REACHABLEVIAWWAN3G

            TelephonyManager.NETWORK_TYPE_LTE -> XNetworkStatus.REACHABLEVIAWWAN4G

            else -> XNetworkStatus.REACHABLEVIAWWAN
        }
    }

    enum class XNetworkStatus {
        UNKNOWN, NOTREACHABLE,
        REACHABLEVIAWWAN,
        REACHABLEVIAWWAN2G, REACHABLEVIAWWAN3G, REACHABLEVIAWWAN4G, REACHABLEVIAWIFI
    }
}

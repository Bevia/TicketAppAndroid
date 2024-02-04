package com.vbevia.montanejosqr.utils

import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NetworkCallbackImpl : ConnectivityManager.NetworkCallback() {

    private var isNetworkAvailable = false

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        isNetworkAvailable = true
        Log.d("BEVIADEBUG NetworkCallback", "Network is available")
        // Handle network availability
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        isNetworkAvailable = false
        Log.d("BEVIADEBUG NetworkCallback", "Network is lost")
        // Handle network loss
    }

    fun isAvailable(): Boolean {
        return isNetworkAvailable
    }
}

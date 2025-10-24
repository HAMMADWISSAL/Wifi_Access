package com.example.wifi_access

import android.Manifest
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks

class MainActivity : AppCompatActivity(), PermissionCallbacks {

    private val NETWORK_STATE = 123
    private lateinit var textStatus: TextView
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textStatus = findViewById(R.id.textStatus)
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    textStatus.text = "Connecté à Internet"
                    textStatus.setTextColor(getColor(android.R.color.holo_green_dark))
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                    textStatus.text = "Hors ligne"
                    textStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                }
            }
        }

        // Check permission
        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_NETWORK_STATE)) {
            registerNetworkCallback()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Accès à l'état du réseau requis.",
                NETWORK_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE
            )
        }
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (requestCode == NETWORK_STATE) registerNetworkCallback()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        textStatus.text = "Permission refusée"
        textStatus.setTextColor(getColor(android.R.color.holo_red_dark))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onStop() {
        super.onStop()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Ignore if not registered
        }
    }
}

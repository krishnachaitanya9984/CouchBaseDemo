package com.example.couchbasedemo.ui.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.couchbase.lite.Database
import com.example.couchbasedemo.database.UserDao
import com.example.couchbasedemo.database.CouchBaseLiteService
import com.example.couchbasedemo.models.ConnectionType
import com.example.couchbasedemo.peertopeer.NearbyClient
import com.example.couchbasedemo.peertopeer.NearbyServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

class PeerToPeerViewModel : ViewModel() {

    var countFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    private var connectionType : MutableStateFlow<ConnectionType> = MutableStateFlow(ConnectionType.NONE)

    fun onServerSelected(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            connectionType.value = ConnectionType.SERVER
            NearbyServer(context).startAdvertising()
        }
    }

    fun onClientSelected(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            connectionType.value = ConnectionType.CLIENT
            NearbyClient(context).startDiscovery()
        }
    }

    fun stop(context: Context) {
        if(connectionType.value == ConnectionType.SERVER) {
            NearbyServer(context).stopAdvertising()
        } else if(connectionType.value == ConnectionType.CLIENT) {
            NearbyClient(context).stopDiscovery()
        }
        connectionType.value = ConnectionType.NONE
    }

    fun addDocument() {
        viewModelScope.launch(Dispatchers.IO) {
            val database: Database = CouchBaseLiteService.database!!
            UserDao.save(database.getCollection(CouchBaseLiteService.COLLECTION)!!)
            val count =
                UserDao.getAllDocuments(database.getCollection(CouchBaseLiteService.COLLECTION)!!)
            countFlow.value = count
        }
    }

    fun removeDocument() {
        viewModelScope.launch(Dispatchers.IO) {
            val database: Database = CouchBaseLiteService.database!!
            UserDao.delete(database.getCollection(CouchBaseLiteService.COLLECTION)!!)
            val count =
                UserDao.getAllDocuments(database.getCollection(CouchBaseLiteService.COLLECTION)!!)
            countFlow.value = count
        }
    }

    fun getAllDocuments() {
        viewModelScope.launch(Dispatchers.IO) {
            val database: Database = CouchBaseLiteService.database!!
            val count =
                UserDao.getAllDocuments(database.getCollection(CouchBaseLiteService.COLLECTION)!!)
            countFlow.value = count
        }
    }

    @SuppressLint("DefaultLocale")
    fun getDeviceIpAddress(context: Context): String? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return getLocalIpAddress()
        }

        return getLocalIpAddress()  // Fallback to get the local IP
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is InetAddress && address.address.size == 4) {
                        return address.hostAddress  // Return the numeric form of the IP
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getPermissions() : List<String> {
        val permissionsList: MutableList<String> = ArrayList()
        permissionsList.add(Manifest.permission.BLUETOOTH)
        permissionsList.add(Manifest.permission.BLUETOOTH_ADMIN)
        permissionsList.add(Manifest.permission.ACCESS_WIFI_STATE)
        permissionsList.add(Manifest.permission.CHANGE_WIFI_STATE)
        permissionsList.add(Manifest.permission.BLUETOOTH_SCAN)
        permissionsList.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionsList.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        return Collections.unmodifiableList<String>(permissionsList)
    }

}
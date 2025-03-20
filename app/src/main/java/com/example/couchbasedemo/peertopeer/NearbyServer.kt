package com.example.couchbasedemo.peertopeer

import android.content.Context
import android.util.Log
import com.example.couchbasedemo.database.CouchBaseLiteService
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

//PassivePeer
class NearbyServer(private val context: Context) {
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        connectionsClient.startAdvertising(
            "NearbyServer",
            SERVICE_ID,
            connectionLifecycleCallback,
            options
        )
            .addOnSuccessListener { Log.d(TAG, "Advertising started") }
            .addOnFailureListener { e -> Log.e(TAG, "Advertising failed", e) }
    }

    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d(TAG, " ConnectionInitiated to: $endpointId")
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Log.d(TAG, "Connected to: $endpointId")
                ioScope.launch {
                    PassivePeer.startListener(
                        CouchBaseLiteService.database!!,
                        endpointId,
                        connectionsClient
                    )
                }
            } else {
                Log.e(TAG, "Connection failed")
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "Disconnected from: $endpointId")
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {
                val receivedData = String(it)
                Log.d(TAG, "Received Data: $receivedData")
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    companion object {
        private const val TAG = "NearbyServer"
        private const val SERVICE_ID = "com.example.couchbasedemo"
    }
}

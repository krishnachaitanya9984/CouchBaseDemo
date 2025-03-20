package com.example.couchbasedemo.peertopeer

import android.content.Context
import android.util.Log
import com.example.couchbasedemo.database.CouchBaseLiteService
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

//ActivePeer
class NearbyClient(private val context: Context) {
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    fun startDiscovery() {
        val options: DiscoveryOptions =
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        connectionsClient.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
            .addOnSuccessListener { unused -> Log.d("NearbyClient", "Discovery started") }
            .addOnFailureListener { e -> Log.e("NearbyClient", "Discovery failed", e) }
    }

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
    }

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                Log.d("NearbyClient", "Endpoint found: $endpointId")
                connectionsClient.requestConnection(
                    "NearbyClient",
                    endpointId,
                    connectionLifecycleCallback
                )
            }


            override fun onEndpointLost(endpointId: String) {
                Log.d("NearbyClient", "Endpoint lost: $endpointId")
            }
        }

    private val connectionLifecycleCallback: ConnectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(
                endpointId: String,
                connectionInfo: ConnectionInfo
            ) {
                Log.d("NearbyClient", "ConnectionInitiated to: $endpointId")
                connectionsClient.acceptConnection(endpointId, payloadCallback)
            }


            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                if (result.getStatus().isSuccess()) {
                    Log.d("NearbyClient", "Connected to: $endpointId")
                    //sendDatabase(endpointId)
                    ioScope.launch {
                        ActivePeer.startReplicator(
                            CouchBaseLiteService.database!!,
                            endpointId,
                            connectionsClient
                        )
                    }

                } else {
                    Log.e("NearbyClient", "Connection failed")
                }
            }

            override fun onDisconnected(endpointId: String) {
                Log.d("NearbyClient", "Disconnected from: $endpointId")
            }
        }

    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.d("NearbyClient", "Payload received")
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
        }

    }

    private fun sendDatabase(endpointId: String) {

        val database = CouchBaseLiteService.database!!
        val collection = database.getCollection(CouchBaseLiteService.COLLECTION)!!
        val doc = collection.getDocument("2")
        doc!!.toJSON()!!.toByteArray(Charsets.UTF_8)
        //val database: Database = CouchBaseLiteService.database!!
        //CRUDOperations.save(database.getCollection(CouchBaseLiteService.COLLECTION)!!)
        //database.(MutableDocument().setString("message", "Hello from Active Peer"))
        val payload: Payload = Payload.fromBytes("Sync Data".toByteArray())
        connectionsClient.sendPayload(endpointId, payload)
    }

    companion object {
        private const val SERVICE_ID = "com.example.couchbasedemo"
    }
}

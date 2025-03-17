package com.example.couchbasedemo.peertopeer

import android.util.Log
import com.couchbase.lite.Database
import com.couchbase.lite.MessageEndpoint
import com.couchbase.lite.MessageEndpointDelegate
import com.couchbase.lite.ProtocolType
import com.couchbase.lite.Replicator
import com.couchbase.lite.ReplicatorChange
import com.couchbase.lite.ReplicatorConfiguration
import com.couchbase.lite.ReplicatorConfigurationFactory
import com.couchbase.lite.ReplicatorType
import com.couchbase.lite.URLEndpoint
import com.couchbase.lite.newConfig
import com.google.android.gms.nearby.connection.ConnectionsClient
import java.net.URI

object ActivePeer {

    fun startReplicator(db: Database, peerId: String, connectionClient: ConnectionsClient) {

        val messageEndpointDelegate = MessageEndpointDelegate { endPoint ->
            ActivePeerConnection(endPoint.uid, connectionClient)
        }

        // Socket EndPoint
        val urlEndPoint = URLEndpoint(URI("wss://192.168.49.1:443/${db.name}"))

        //PeerId Endpoint
        val messageEndPoint = MessageEndpoint(
            peerId,
            "active",
            ProtocolType.MESSAGE_STREAM,
            messageEndpointDelegate
        )

        val replicator = Replicator(
            ReplicatorConfigurationFactory.newConfig(
                target = messageEndPoint,
                collections = mapOf(db.collections to null),
                // Set replicator type
                type = ReplicatorType.PUSH_AND_PULL

            )
        )

        replicator.addChangeListener { change: ReplicatorChange ->
            Log.e("ActivePeer", "Replicator change: ${change.status}")
        }

        replicator.start(false)

    }
}
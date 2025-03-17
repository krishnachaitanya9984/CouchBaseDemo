package com.example.couchbasedemo.peertopeer


import com.couchbase.lite.Database
import com.couchbase.lite.MessageEndpointListener
import com.couchbase.lite.MessageEndpointListenerConfigurationFactory
import com.couchbase.lite.ProtocolType
import com.couchbase.lite.URLEndpointListener
import com.couchbase.lite.URLEndpointListenerConfigurationFactory
import com.couchbase.lite.newConfig
import com.google.android.gms.nearby.connection.ConnectionsClient

object PassivePeer {

    fun startListener(db: Database, peerId: String, connectionClient: ConnectionsClient) {

        val listener = URLEndpointListener(
            URLEndpointListenerConfigurationFactory.newConfig(
                collections = db.collections
            )
        )

        listener.start()

        val connection = PassivePeerConnection(peerId, connectionClient)
        val messageEndPointListener = MessageEndpointListener(
            MessageEndpointListenerConfigurationFactory.newConfig(
                db.collections,
                ProtocolType.MESSAGE_STREAM
            )
        )
        messageEndPointListener.accept(connection)

    }

}

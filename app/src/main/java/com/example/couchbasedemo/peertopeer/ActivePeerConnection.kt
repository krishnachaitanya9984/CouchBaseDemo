package com.example.couchbasedemo.peertopeer

import android.util.Log
import com.couchbase.lite.Message
import com.couchbase.lite.MessageEndpointConnection
import com.couchbase.lite.MessagingCloseCompletion
import com.couchbase.lite.MessagingCompletion
import com.couchbase.lite.ReplicatorConnection
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ActivePeerConnection(
    private val peerId: String,
    private val connectionClient: ConnectionsClient
) : MessageEndpointConnection {

    private val job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)
    private var replicatorConnection: ReplicatorConnection? = null

    override fun open(connection: ReplicatorConnection, completion: MessagingCompletion) {
        Log.e("ActivePeerConnection", "MessageEndpointConnection opened.")
        replicatorConnection = connection
        completion.complete(true, null)
    }

    override fun send(message: Message, completion: MessagingCompletion) {
        ioScope.launch {
            connectionClient.sendPayload(peerId, Payload.fromBytes(message.toData())).apply {
                addOnFailureListener {}
                addOnCanceledListener {}
                addOnCompleteListener {}
                addOnSuccessListener {
                    replicatorConnection?.receive(message)
                    Log.d("ActivePeerConnection", "Payload successfully sent")
                    completion.complete(true, null)
                }
            }
        }
    }

    override fun close(error: Exception?, completion: MessagingCloseCompletion) {
        Log.e("ActivePeerConnection", "Connection closed")
        completion.complete()
    }
}
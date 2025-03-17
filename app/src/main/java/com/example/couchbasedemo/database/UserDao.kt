package com.example.couchbasedemo.database

import android.util.Log
import com.couchbase.lite.MutableDocument
import com.example.couchbasedemo.models.User
import com.couchbase.lite.Collection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object UserDao {

    private var uniqueId = 0

    private fun getNextId(): Int {
        if (uniqueId <= 0) {
            uniqueId = 0
        }
        uniqueId += 1
        return uniqueId
    }

    private fun getPreviousId(): Int {
        uniqueId -= 1
        return uniqueId
    }

    suspend fun save(collection: Collection) = withContext(Dispatchers.IO) {
        val user = User(
            id = getNextId(),
            name = "Alex$uniqueId",
            email = "alex123@gmail.com"
        )

        val userJson = Json.encodeToString(User.serializer(), user)
        val newUserDoc = MutableDocument(user.id.toString(), userJson)
        try {
            collection.save(newUserDoc)
        } catch (e: Exception) {
            Log.e("UserDao", "Error deleting document: ${e.message}")
        }

    }

    suspend fun delete(collection: Collection) = withContext(Dispatchers.IO) {
        if (uniqueId > 0) {
            val deletedDoc = collection.getDocument(uniqueId.toString())
            try {
                collection.delete(deletedDoc!!)
                getPreviousId()
            } catch (e: Exception) {
                Log.e("UserDao", "Error deleting document: ${e.message}")
            }
        }

    }

    suspend fun getAllDocuments(collection: Collection): Int = withContext(Dispatchers.IO) {
        uniqueId = collection.count.toInt()
        return@withContext uniqueId
    }


}
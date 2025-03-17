package com.example.couchbasedemo.database

import android.content.Context
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import com.couchbase.lite.DatabaseConfigurationFactory
import com.couchbase.lite.LogLevel
import com.couchbase.lite.newConfig

object CouchBaseLiteService {
    var database: Database? = null
    private const val DATABASE_NAME = "CouchbaseDemoDB"
    const val COLLECTION = "User"
    private var config: DatabaseConfiguration? = null

    fun initDB(context: Context) {
        CouchbaseLite.init(context)
        val config = DatabaseConfigurationFactory.newConfig(
            context.filesDir.absolutePath + "/" + DATABASE_NAME
        )
        database = Database(DATABASE_NAME, config)
        database!!.createCollection(COLLECTION)
        Database.log.console.level = LogLevel.VERBOSE
    }

    fun closeDB() {
        config?.let {
            val db = Database(this.DATABASE_NAME, config!!)
            db.close()
        }

    }
}
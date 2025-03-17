package com.example.couchbasedemo

import android.app.Application

class CouchBaseDemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        System.setProperty("javax.net.debug", "ssl")

    }
}


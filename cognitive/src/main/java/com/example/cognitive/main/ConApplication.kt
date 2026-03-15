package com.example.cognitive.main

import android.app.Application
import android.content.Context
import bind_device.BindStatusManager
import user.UserManager

class ConApplication: Application() {

    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        BindStatusManager.init(this)
        UserManager.init(this)
    }
}
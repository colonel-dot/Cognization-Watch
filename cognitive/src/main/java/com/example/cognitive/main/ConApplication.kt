package com.example.cognitive.main

import android.app.Application
import bind_device.BindStatusManager
import user.UserManager

class ConApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        BindStatusManager.init(this)
        UserManager.init(this)
    }
}
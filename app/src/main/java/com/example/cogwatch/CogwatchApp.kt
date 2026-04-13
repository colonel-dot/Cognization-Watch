package com.example.cogwatch

import android.app.Application
import android.util.Log
import com.alibaba.android.arouter.BuildConfig
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.bind_device.BindStatusManager
import com.example.common.login.GuestStateHolder
import com.example.common.persistense.AppDatabase
import user.UserManager

class CogwatchApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Log.e("ARouterDebug", "Application onCreate")
//        if (BuildConfig.DEBUG) {
//            ARouter.openLog()
//            ARouter.openDebug()
//        }
        ARouter.init(this)
        Log.e("ARouterDebug", "ARouter init finished")

        AppDatabase.init(this)

        BindStatusManager.init(this)
        GuestStateHolder.init(this)
        UserManager.init(this)
    }
}
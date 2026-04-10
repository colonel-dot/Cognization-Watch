package com.example.cogwatch

import android.app.Application
import android.util.Log
import com.alibaba.android.arouter.BuildConfig
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.bind_device.BindStatusManager
import com.example.common.persistense.AppDatabase
import user.UserManager

class CogwatchApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Log.e("ARouterDebug", "Application onCreate")
        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug() // TODO: ARouter debug
        }
        ARouter.init(this)
        Log.e("ARouterDebug", "ARouter init finished")

        // 初始化 AppDatabase
        AppDatabase.init(this)

        // 初始化 Manager 类
        BindStatusManager.init(this)
        UserManager.init(this)
    }
}
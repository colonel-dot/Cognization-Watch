package com.example.cogwatch

import android.app.Application
import android.util.Log
import com.alibaba.android.arouter.BuildConfig
import com.alibaba.android.arouter.launcher.ARouter
import com.example.cognitive.main.ConApplication
import bind_device.BindStatusManager
import user.UserManager

class CogwatchApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Log.e("ARouterDebug", "Application onCreate")
        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)
        Log.e("ARouterDebug", "ARouter init finished")

        // 初始化 cognitive 模块的 ConApplication.context
        ConApplication.context = applicationContext

        // 初始化 cognitive 模块的 Manager 类
        BindStatusManager.init(this)
        UserManager.init(this)
    }
}
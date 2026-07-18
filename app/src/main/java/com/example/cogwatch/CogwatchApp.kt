package com.example.cogwatch

import android.app.Application
import android.os.Trace
import android.util.Log

/**
 * Application entry point.
 *
 * All component initialization (ARouter, AppDatabase, BindStatusManager,
 * GuestStateHolder, UserManager, TRTC) is delegated to the androidx.startup
 * Initializer chain. See app/.../startup/ for individual Initializer classes.
 *
 * Key optimization: AppDatabase.init() only caches the ApplicationContext;
 * the expensive Room.databaseBuilder().build() is deferred until the first
 * DAO access, removing it from the cold-start critical path.
 */
class CogwatchApp : Application() {
    override fun onCreate() {
        Trace.beginSection("CogwatchApp_onCreate")
        super.onCreate()
        Log.d("CogwatchApp", "启动任务委托给startup初始化器")
        Trace.endSection()
    }
}

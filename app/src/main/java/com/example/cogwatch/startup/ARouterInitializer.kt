package com.example.cogwatch.startup

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import androidx.tracing.Trace
import com.alibaba.android.arouter.launcher.ARouter

/**
 * Initializes ARouter navigation framework via androidx.startup.
 * Must run before any Activity uses @Route / ARouter.getInstance().navigation().
 */
class ARouterInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Trace.beginSection("ARouterInit")
        Log.d("Startup", "ARouter init start")
        ARouter.init(context.applicationContext as android.app.Application)
        Log.d("Startup", "ARouter init finished")
        Trace.endSection()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

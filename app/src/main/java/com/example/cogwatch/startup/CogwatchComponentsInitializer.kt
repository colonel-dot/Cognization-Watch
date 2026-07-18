package com.example.cogwatch.startup

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import androidx.tracing.Trace
import com.example.common.bind_device.BindStatusManager
import com.example.common.login.GuestStateHolder
import com.example.cognitive.user.UserManager

/**
 * Initializes lightweight app components (SharedPreferences-backed state holders).
 * Depends on BindStatusManager being ready before UserManager.
 */
class CogwatchComponentsInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Trace.beginSection("CogwatchComponentsInit")
        Log.d("Startup", "Components init start")

        BindStatusManager.init(context)
        GuestStateHolder.init(context)
        // UserManager reads from BindStatusManager and LoginStatusManager,
        // so BindStatusManager must be initialized first.
        UserManager.init(context)

        Log.d("Startup", "Components init finished")
        Trace.endSection()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

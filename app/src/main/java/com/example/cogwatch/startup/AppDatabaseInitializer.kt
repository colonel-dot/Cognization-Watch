package com.example.cogwatch.startup

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import androidx.tracing.Trace
import com.example.common.persistense.AppDatabase

/**
 * Caches the ApplicationContext for AppDatabase without building the Room database.
 * The actual Room.databaseBuilder().build() is deferred until the first DAO access.
 *
 * This is the single largest startup optimization: Room DB build moves
 * from Application.onCreate() (cold-start critical path) to first-use time.
 */
class AppDatabaseInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Trace.beginSection("AppDatabaseInit")
        Log.d("Startup", "AppDatabase init (context cache only, no DB build)")
        AppDatabase.init(context)
        Trace.endSection()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

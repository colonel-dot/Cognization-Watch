package com.example.common.persistense

import android.content.Context
import android.os.Trace
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.common.persistense.behavior.DailyBehaviorDao
import com.example.common.persistense.behavior.DailyBehaviorEntity
import com.example.common.persistense.geofence.GeofenceItem
import com.example.common.persistense.geofence.GeofenceItemDao
import com.example.common.persistense.risk.DailyRiskDao
import com.example.common.persistense.risk.DailyRiskEntity

@TypeConverters(LocalDateConverter::class)
@Database(
    entities = [
        DailyBehaviorEntity::class,
        DailyRiskEntity::class,
        GeofenceItem::class
    ],
    version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyBehaviorDao(): DailyBehaviorDao
    abstract fun dailyRiskDao(): DailyRiskDao
    abstract fun geofenceItemDao(): GeofenceItemDao
    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private var appContext: Context? = null

        /**
         * Lazy init — only caches the ApplicationContext.
         * The actual Room .build() is deferred until [getDatabase] is first called.
         */
        @Synchronized
        fun init(context: Context) {
            appContext = context.applicationContext
        }

        /**
         * Returns the AppDatabase singleton, building it on first access (double-checked locking).
         */
        fun getDatabase(context: Context? = null): AppDatabase {
            instance?.let { return it }
            synchronized(this) {
                instance?.let { return it }
                val ctx = context?.applicationContext
                    ?: appContext
                    ?: throw IllegalStateException("AppDatabase not initialized. Call init() first.")
                Trace.beginSection("AppDatabase Build")
                return try {
                    Room.databaseBuilder(
                        ctx,
                        AppDatabase::class.java,
                        "daily_behavior_database"
                    ).build().apply { instance = this }
                } finally {
                    Trace.endSection()
                }
            }
        }

        fun getInstance(): AppDatabase {
            return instance ?: throw IllegalStateException("AppDatabase not initialized. Call init() first.")
        }

        fun getAppContext(): Context {
            return appContext ?: throw IllegalStateException("AppDatabase context not initialized")
        }
    }
}
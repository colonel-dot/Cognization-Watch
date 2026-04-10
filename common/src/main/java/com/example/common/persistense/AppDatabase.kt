package com.example.common.persistense

import android.content.Context
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
        private var instance : AppDatabase ?= null
        private var appContext: Context? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(context.applicationContext,
                AppDatabase::class.java, "daily_behavior_database")
                .build().apply { instance = this }
        }

        @Synchronized
        fun init(context: Context) {
            appContext = context.applicationContext
            getDatabase(context)
        }

        fun getInstance(): AppDatabase {
            return instance ?: throw IllegalStateException("AppDatabase not initialized. Call init() first.")
        }

        fun getAppContext(): Context {
            return appContext ?: throw IllegalStateException("AppDatabase context not initialized")
        }
    }
}
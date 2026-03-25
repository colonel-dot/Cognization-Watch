package com.example.common.persistense.geofence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GeofenceItem::class], version = 1)
abstract class GeofenceDatabase : RoomDatabase() {
    abstract fun geofenceItemDao(): GeofenceItemDao

    companion object {
        @Volatile
        private var instance: GeofenceDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): GeofenceDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                context.applicationContext,
                GeofenceDatabase::class.java,
                "geofence_database"
            ).build().apply {
                instance = this
            }
        }
    }
}
package com.example.common.persistense

import android.content.Context
import kotlinx.coroutines.runBlocking

object BusinessDataManager {

    fun clearAll(context: Context) {
        val db = AppDatabase.getDatabase(context)
        runBlocking {
            db.dailyBehaviorDao().deleteAll()
            db.dailyRiskDao().deleteAll()
            db.geofenceItemDao().deleteAll()
        }
    }
}

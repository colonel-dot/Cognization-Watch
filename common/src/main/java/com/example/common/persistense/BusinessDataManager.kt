package com.example.common.persistense

import android.content.Context
import kotlinx.coroutines.runBlocking

/**
 * 业务数据管理工具类
 * 用于清除所有业务数据（行为记录、风险记录、地理围栏记录）
 */
object BusinessDataManager {

    /**
     * 清除所有业务数据
     */
    fun clearAll(context: Context) {
        val db = AppDatabase.getDatabase(context)
        runBlocking {
            db.dailyBehaviorDao().deleteAll()
            db.dailyRiskDao().deleteAll()
            db.geofenceItemDao().deleteAll()
        }
    }
}

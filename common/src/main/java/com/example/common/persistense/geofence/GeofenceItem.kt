package com.example.common.persistense.geofence

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geofence_event")
data class GeofenceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Int, // 事件发生的时间戳（1970到现在的分钟值）
    val lat: Double,    // 纬度
    val lng: Double,    // 经度
    val status: Int     // 状态码
) {
    companion object {
        const val STATUS_IN = 1      // 进入围栏
        const val STATUS_OUT = 2     // 离开围栏
        const val STATUS_STAYED = 3  // 停留在围栏内10分钟
        const val STATUS_LOCFAIL = 4 // 定位失败
        const val STATUS_UNKNOWN = 0 // 未知状态
    }
}
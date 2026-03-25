package com.example.common.persistense.behavior

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_behavior")
data class DailyBehaviorEntity (    @PrimaryKey val date: LocalDate,

                                    val wakeMinute: Int? = 0, // 起床事件
                                    val sleepMinute: Int? = 0, // 睡觉时间

                                    val schulte16TimeSec: Double? = 0.0, // 舒尔特16格方格通关时间
                                    val schulte25TimeSec: Double? = 0.0, // 舒尔特25格方格通关时间
                                    val speechScore: Double? = 0.0, // 语音识别评分

                                    val steps: Int? = 0, // 每日步数
                                    val activeTime: Int? = 0, // 运动时间，单位秒
                                    val restTime: Int? = 0)



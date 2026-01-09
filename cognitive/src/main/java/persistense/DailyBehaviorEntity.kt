package persistense

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_behavior")
data class DailyBehaviorEntity (    @PrimaryKey val date: LocalDate,

                                    val wakeMinute: Int?,
                                    val sleepMinute: Int?,

                                    val schulteTimeSec: Double?,//舒尔特方格通关时间
                                    val speechScore: Double?,//语音识别评分

                                    val steps: Int?)



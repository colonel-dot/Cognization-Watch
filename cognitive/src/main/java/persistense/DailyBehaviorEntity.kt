package persistense

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_behavior")
data class DailyBehaviorEntity (    @PrimaryKey val date: LocalDate,

                                    val wakeMinute: Int? = 0,
                                    val sleepMinute: Int? = 0,

                                    val schulteTimeSec: Double? = 0.0,//舒尔特方格通关时间
                                    val speechScore: Double? = 0.0,//语音识别评分

                                    val steps: Int? = 0,
                                    val activeTime: Int? = 0,//运动时间，单位秒
                                    val restTime: Int? = 0)



package debug_simulate

import android.content.Context
import persistense.AppDatabase
import persistense.DailyBehaviorEntity
import java.time.LocalDate

object InsertData {
    private val testDate1 = LocalDate.of(2026, 3, 8)
    private val testDate2 = LocalDate.of(2026, 3, 9)
    private val testDate3 = LocalDate.of(2026, 3, 10)
    private val testDate11 = LocalDate.of(2026, 3, 11)
    private val testDate12 = LocalDate.of(2026, 3, 12)
    private val testDate13 = LocalDate.of(2026, 3, 13)
    private val testDate14 = LocalDate.of(2026, 3, 14)
    private val testDate15 = LocalDate.of(2026, 3, 15)
    private val testDate16 = LocalDate.of(2026, 3, 16)
    private val testEntity1 = DailyBehaviorEntity(
        date = testDate1,
        wakeMinute = 480, // 8:00 → 8*60=480分钟
        sleepMinute = 1140, // 19:00 → 19*60=1140分钟
        schulte16TimeSec = 12500.0,
        schulte25TimeSec = 25800.0,
        speechScore = 95.0,
        steps = 8000,
        activeTime = 1800, // 30分钟
        restTime = 3600 // 60分钟
    )

    private val testEntity2 = DailyBehaviorEntity(
        date = testDate2,
        wakeMinute = 510, // 8:30
        sleepMinute = 1170, // 19:30
        schulte16TimeSec = 11200.0,
        schulte25TimeSec = 24300.0,
        speechScore = 92.5,
        steps = 9500,
        activeTime = 2400, // 40分钟
        restTime = 3000 // 50分钟
    )

    private val testEntity3 = DailyBehaviorEntity(
        date = testDate3,
        wakeMinute = 450, // 7:30
        sleepMinute = 1200, // 20:00
        schulte16TimeSec = 10800.0,
        schulte25TimeSec = 22100.0,
        speechScore = 98.0,
        steps = 12000,
        activeTime = 3000, // 50分钟
        restTime = 2400 // 40分钟
    )

    private val testEntity11 = DailyBehaviorEntity(
        date = testDate11,
        wakeMinute = 450, // 7:30
        sleepMinute = 1200, // 20:00
        schulte16TimeSec = 10800.00,
        schulte25TimeSec = 22100.0,
        speechScore = 98.0,
        steps = 12000,
        activeTime = 3000, // 50分钟
        restTime = 2400 // 40分钟
    )

    private val testEntity12 = DailyBehaviorEntity(
        date = testDate12,
        wakeMinute = 450, // 7:30
        sleepMinute = 1200, // 20:00
        schulte16TimeSec = 10800.0,
        schulte25TimeSec = 22100.0,
        speechScore = 98.0,
        steps = 12000,
        activeTime = 3000, // 50分钟
        restTime = 2400 // 40分钟
    )

    suspend fun insertData(context: Context) {
        val db = AppDatabase.getDatabase(context)
        val dao = db.dailyBehaviorDao()
        dao.insert(testEntity1)
        dao.insert(testEntity2)
        dao.insert(testEntity3)
        dao.insert(testEntity11)
        dao.insert(testEntity12)
    }

}
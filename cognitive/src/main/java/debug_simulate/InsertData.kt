package debug_simulate

import android.content.Context
import com.example.common.persistense.AppDatabase
import com.example.common.persistense.behavior.DailyBehaviorEntity
import com.example.common.persistense.risk.DailyRiskEntity
import java.time.LocalDate

object InsertData {

    lateinit var db: AppDatabase

    private val testDate1 = LocalDate.of(2026, 3, 8)
    private val testDate2 = LocalDate.of(2026, 3, 9)
    private val testDate3 = LocalDate.of(2026, 3, 10)
    private val testDate11 = LocalDate.of(2026, 3, 11)
    private val testDate12 = LocalDate.of(2026, 3, 12)
    private val testDate17 = LocalDate.of(2026, 3, 17)
    private val testDateToday = LocalDate.now()

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

    private val riskEntity1 = DailyRiskEntity(
        date = testDate1,
        riskScore = 0.4
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

    private val riskEntity2 = DailyRiskEntity(
        date = testDate2,
        riskScore = 0.35
    )

    val testEntity3 = DailyBehaviorEntity(
        date = testDate3,
        wakeMinute = 490, // 7:30
        sleepMinute = 1200, // 20:00
        schulte16TimeSec = 10800.0,
        schulte25TimeSec = 22100.0,
        speechScore = 98.0,
        steps = 12000,
        activeTime = 3000, // 50分钟
        restTime = 2400 // 40分钟
    )

    val testEntity17 = DailyBehaviorEntity(
        date = testDate17,
        wakeMinute = 490, // 7:30
        sleepMinute = 1200, // 20:00
        schulte16TimeSec = 10800.0,
        schulte25TimeSec = 22100.0,
        speechScore = 98.0,
        steps = 12000,
        activeTime = 3000, // 50分钟
        restTime = 2400 // 40分钟
    )

    val testEntityToday = DailyBehaviorEntity(
        date = testDateToday,
        wakeMinute = 400, // 6:00
        sleepMinute = 1200, // 20:00
        schulte16TimeSec = 10800.0,
        schulte25TimeSec = 22100.0,
        speechScore = 98.0,
        steps = 12000,
        activeTime = 3000, // 50分钟
        restTime = 2400 // 40分钟
    )

    val riskEntityToday = DailyRiskEntity(
        date = testDateToday,
        riskScore = 0.33
    )

    val riskEntity17 = DailyRiskEntity(
        date = testDate17,
        riskScore = 0.33
    )

    val riskEntity3 = DailyRiskEntity(
        date = testDate3,
        riskScore = 0.2
    )

    val testEntity11 = DailyBehaviorEntity(
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

    val riskEntity11 = DailyRiskEntity(
        date = testDate3,
        riskScore = 0.7
    )

    val testEntity12 = DailyBehaviorEntity(
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

    val riskEntity12 = DailyRiskEntity(
        date = testDate3,
        riskScore = 0.33
    )

    private val riskEntity7 = DailyRiskEntity(
        date = LocalDate.now(),
        riskScore = 0.7
    )

    fun init(context: Context) {
        db = AppDatabase.getDatabase(context)
    }

    suspend fun insertBehaviorData() {
        val dao = db.dailyBehaviorDao()
        dao.insert(testEntity1)
        dao.insert(testEntity2)
        dao.insert(testEntity3)
        //dao.insert(testEntity11)
        dao.insert(testEntity12)
        dao.insert(testEntityToday)
    }

    suspend fun  insertRiskData() {
        val riskDao = db.dailyRiskDao()
        riskDao.insert(riskEntity1)
        riskDao.insert(riskEntity2)
        riskDao.insert(riskEntity3)
        riskDao.insert(riskEntity7)
        //riskDao.insert(riskEntity11)
        riskDao.insert(riskEntity12)
        riskDao.insert(riskEntity17)
        riskDao.insert(riskEntityToday)
    }

}
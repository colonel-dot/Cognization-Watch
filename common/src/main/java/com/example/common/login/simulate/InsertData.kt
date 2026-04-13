package com.example.common.login.simulate

import android.content.Context
import android.util.Log
import com.example.common.persistense.AppDatabase
import com.example.common.persistense.behavior.DailyBehaviorEntity
import com.example.common.persistense.geofence.GeofenceItem
import com.example.common.persistense.risk.DailyRiskEntity
import com.example.common.persistense.risk.RiskLevel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object InsertData {

    private const val TAG = "InsertData"
    lateinit var db: AppDatabase

    private val today = LocalDate.now()

    // 生成从今天往前推 15 天的日期列表
    private val dates = (0..14).map { today.minusDays(it.toLong()) }

    // 15 天的行为数据
    private val behaviorEntities = listOf(
        DailyBehaviorEntity(date = dates[0],  wakeMinute = 400, sleepMinute = 1200, schulte16TimeSec = 10800.0, schulte25TimeSec = 22100.0, speechScore = 98.0,  steps = 12000, activeTime = 3000, restTime = 2400),
        DailyBehaviorEntity(date = dates[1],  wakeMinute = 420, sleepMinute = 1215, schulte16TimeSec = 11200.0, schulte25TimeSec = 23500.0, speechScore = 96.5, steps = 11000, activeTime = 2700, restTime = 2500),
        DailyBehaviorEntity(date = dates[2],  wakeMinute = 450, sleepMinute = 1190, schulte16TimeSec = 11800.0, schulte25TimeSec = 24000.0, speechScore = 94.0, steps = 9500,  activeTime = 2400, restTime = 2700),
        DailyBehaviorEntity(date = dates[3],  wakeMinute = 390, sleepMinute = 1170, schulte16TimeSec = 10500.0, schulte25TimeSec = 21800.0, speechScore = 97.0, steps = 13000, activeTime = 3200, restTime = 2100),
        DailyBehaviorEntity(date = dates[4],  wakeMinute = 480, sleepMinute = 1230, schulte16TimeSec = 12000.0, schulte25TimeSec = 25000.0, speechScore = 93.0, steps = 8500,  activeTime = 2100, restTime = 3000),
        DailyBehaviorEntity(date = dates[5],  wakeMinute = 430, sleepMinute = 1200, schulte16TimeSec = 11000.0, schulte25TimeSec = 22800.0, speechScore = 95.5, steps = 10500, activeTime = 2800, restTime = 2300),
        DailyBehaviorEntity(date = dates[6],  wakeMinute = 460, sleepMinute = 1180, schulte16TimeSec = 11500.0, schulte25TimeSec = 23800.0, speechScore = 94.5, steps = 9000,  activeTime = 2500, restTime = 2600),
        DailyBehaviorEntity(date = dates[7],  wakeMinute = 410, sleepMinute = 1210, schulte16TimeSec = 10900.0, schulte25TimeSec = 22300.0, speechScore = 96.0, steps = 11500, activeTime = 2900, restTime = 2200),
        DailyBehaviorEntity(date = dates[8],  wakeMinute = 440, sleepMinute = 1195, schulte16TimeSec = 11300.0, schulte25TimeSec = 23200.0, speechScore = 95.0, steps = 10000, activeTime = 2600, restTime = 2400),
        DailyBehaviorEntity(date = dates[9],  wakeMinute = 470, sleepMinute = 1220, schulte16TimeSec = 11900.0, schulte25TimeSec = 24500.0, speechScore = 93.5, steps = 8800,  activeTime = 2300, restTime = 2800),
        DailyBehaviorEntity(date = dates[10], wakeMinute = 420, sleepMinute = 1200, schulte16TimeSec = 11100.0, schulte25TimeSec = 23000.0, speechScore = 96.0, steps = 10800, activeTime = 2750, restTime = 2450),
        DailyBehaviorEntity(date = dates[11], wakeMinute = 455, sleepMinute = 1185, schulte16TimeSec = 11600.0, schulte25TimeSec = 24200.0, speechScore = 94.0, steps = 9200,  activeTime = 2450, restTime = 2650),
        DailyBehaviorEntity(date = dates[12], wakeMinute = 405, sleepMinute = 1205, schulte16TimeSec = 10700.0, schulte25TimeSec = 22000.0, speechScore = 97.5, steps = 12500, activeTime = 3100, restTime = 2050),
        DailyBehaviorEntity(date = dates[13], wakeMinute = 435, sleepMinute = 1195, schulte16TimeSec = 11400.0, schulte25TimeSec = 23600.0, speechScore = 95.5, steps = 10200, activeTime = 2700, restTime = 2350),
        DailyBehaviorEntity(date = dates[14], wakeMinute = 450, sleepMinute = 1180, schulte16TimeSec = 11700.0, schulte25TimeSec = 24100.0, speechScore = 94.5, steps = 9800,  activeTime = 2550, restTime = 2550)
    )

    // 15 天的风险数据
    // riskScore → riskLevel: 0.0-0.3=正常, 0.3-0.5=轻度风险, 0.5-1.0=明显下滑
    private val riskEntities = listOf(
        DailyRiskEntity(date = dates[0],  riskScore = 0.33, riskLevel = RiskLevel.认知情况正常,          sleepRisk = 0.25, schulteRisk = 0.30, stepsRisk = 0.35, speechRisk = 0.42, explanations = "今日认知表现正常，各项指标均在安全范围内。继续保持规律作息和适度锻炼。"),
        DailyRiskEntity(date = dates[1],  riskScore = 0.38, riskLevel = RiskLevel.认知情况正常,          sleepRisk = 0.32, schulteRisk = 0.35, stepsRisk = 0.40, speechRisk = 0.45, explanations = "睡眠质量略有波动，建议关注睡前活动安排。整体认知状态良好。"),
        DailyRiskEntity(date = dates[2],  riskScore = 0.45, riskLevel = RiskLevel.认知情况存在轻度风险,   sleepRisk = 0.40, schulteRisk = 0.42, stepsRisk = 0.48, speechRisk = 0.50, explanations = "步数有所减少，建议增加日间活动量。认知表现略低于平日平均水平。"),
        DailyRiskEntity(date = dates[3],  riskScore = 0.28, riskLevel = RiskLevel.认知情况正常,          sleepRisk = 0.20, schulteRisk = 0.25, stepsRisk = 0.30, speechRisk = 0.37, explanations = "各项指标均处于良好区间。今日认知表现优异，请继续保持。"),
        DailyRiskEntity(date = dates[4],  riskScore = 0.52, riskLevel = RiskLevel.认知能力有明显下滑趋势, sleepRisk = 0.55, schulteRisk = 0.50, stepsRisk = 0.48, speechRisk = 0.55, explanations = "睡眠风险指数偏高，注意休息。认知表现有下降趋势，建议关注。"),
        DailyRiskEntity(date = dates[5],  riskScore = 0.40, riskLevel = RiskLevel.认知情况存在轻度风险,   sleepRisk = 0.38, schulteRisk = 0.40, stepsRisk = 0.42, speechRisk = 0.40, explanations = "整体状态平稳，无明显异常。继续维持健康生活方式。"),
        DailyRiskEntity(date = dates[6],  riskScore = 0.47, riskLevel = RiskLevel.认知情况存在轻度风险,   sleepRisk = 0.45, schulteRisk = 0.48, stepsRisk = 0.45, speechRisk = 0.50, explanations = "语音能力略有波动，建议多进行语言交流活动。认知状态尚可。"),
        DailyRiskEntity(date = dates[7],  riskScore = 0.31, riskLevel = RiskLevel.认知情况正常,          sleepRisk = 0.28, schulteRisk = 0.30, stepsRisk = 0.32, speechRisk = 0.34, explanations = "今日认知表现正常，各项指标稳定。请保持现有作息规律。"),
        DailyRiskEntity(date = dates[8],  riskScore = 0.42, riskLevel = RiskLevel.认知情况存在轻度风险,   sleepRisk = 0.40, schulteRisk = 0.42, stepsRisk = 0.40, speechRisk = 0.46, explanations = "舒尔特方格表现略低于预期，建议适当进行注意力训练。"),
        DailyRiskEntity(date = dates[9],  riskScore = 0.55, riskLevel = RiskLevel.认知能力有明显下滑趋势, sleepRisk = 0.58, schulteRisk = 0.55, stepsRisk = 0.50, speechRisk = 0.57, explanations = "多项指标出现异常，请关注老人状态，必要时建议就医评估。"),
        DailyRiskEntity(date = dates[10], riskScore = 0.36, riskLevel = RiskLevel.认知情况正常,          sleepRisk = 0.30, schulteRisk = 0.35, stepsRisk = 0.38, speechRisk = 0.41, explanations = "认知状态正常，睡眠质量良好。继续保持规律生活。"),
        DailyRiskEntity(date = dates[11], riskScore = 0.48, riskLevel = RiskLevel.认知情况存在轻度风险,   sleepRisk = 0.48, schulteRisk = 0.45, stepsRisk = 0.50, speechRisk = 0.49, explanations = "步数风险偏高，建议增加户外活动时间。整体认知表现尚可。"),
        DailyRiskEntity(date = dates[12], riskScore = 0.29, riskLevel = RiskLevel.认知情况正常,          sleepRisk = 0.22, schulteRisk = 0.28, stepsRisk = 0.30, speechRisk = 0.36, explanations = "各项指标均为正常范围，今日认知表现优秀。继续保持。"),
        DailyRiskEntity(date = dates[13], riskScore = 0.41, riskLevel = RiskLevel.认知情况存在轻度风险,   sleepRisk = 0.38, schulteRisk = 0.40, stepsRisk = 0.43, speechRisk = 0.43, explanations = "语音能力评分略有波动，建议多进行语言交流练习。"),
        DailyRiskEntity(date = dates[14], riskScore = 0.46, riskLevel = RiskLevel.认知情况存在轻度风险,   sleepRisk = 0.44, schulteRisk = 0.46, stepsRisk = 0.47, speechRisk = 0.47, explanations = "整体状态平稳，各项风险指标接近正常上限，建议持续关注。")
    )

    const val HOME_LAT = 34.3416
    const val HOME_LNG = 108.9398

    private const val GARDEN_LAT = 34.3420
    private const val GARDEN_LNG = 108.9402

    private const val HOSPITAL_LAT = 34.3408
    private const val HOSPITAL_LNG = 108.9389

    private const val MARKET_LAT = 34.3430
    private const val MARKET_LNG = 108.9410

    private const val CENTER_LAT = 34.3410
    private const val CENTER_LNG = 108.9392

    private val timeFloats = listOf(-5, 3, -2, 7, -4, 1, -3)

    private fun dateTimeToMinutes(year: Int, month: Int, day: Int, hour: Int, minute: Int): Int {
        val zone = ZoneId.systemDefault()
        var adjHour = hour
        var adjMin = minute
        while (adjMin < 0) {
            adjMin += 60
            adjHour -= 1
        }
        while (adjMin >= 60) {
            adjMin -= 60
            adjHour += 1
        }
        val localDateTime = LocalDateTime.of(year, month, day, adjHour, adjMin)
        return (localDateTime.atZone(zone).toInstant().toEpochMilli() / 60000).toInt()
    }

    private val geofenceEntities: List<GeofenceItem> = run {
        val items = mutableListOf<GeofenceItem>()
        for (dayOffset in 0..6) {
            val date = today.minusDays(dayOffset.toLong())
            val year = date.year
            val month = date.monthValue
            val day = date.dayOfMonth
            val float = timeFloats[dayOffset]

            // 场景1：早上7点出门散步，小区花园待1小时后回家
            val gardenInTs  = dateTimeToMinutes(year, month, day, 7, 0 + float)
            val gardenOutTs = dateTimeToMinutes(year, month, day, 8, 0 + float)
            items.add(GeofenceItem(timestamp = gardenInTs,  lat = GARDEN_LAT, lng = GARDEN_LNG, status = GeofenceItem.STATUS_IN))
            items.add(GeofenceItem(timestamp = gardenOutTs, lat = GARDEN_LAT, lng = GARDEN_LNG, status = GeofenceItem.STATUS_OUT))

            // 场景2：上午9点左右去菜市场买菜，10点回家
            val marketInTs  = dateTimeToMinutes(year, month, day, 9, 0 + float)
            val marketOutTs = dateTimeToMinutes(year, month, day, 10, 0 + float)
            items.add(GeofenceItem(timestamp = marketInTs,  lat = MARKET_LAT, lng = MARKET_LNG, status = GeofenceItem.STATUS_IN))
            items.add(GeofenceItem(timestamp = marketOutTs, lat = MARKET_LAT, lng = MARKET_LNG, status = GeofenceItem.STATUS_OUT))

            // 场景3：下午3点左右去社区医院，4点回家
            if (dayOffset % 2 == 0) {
                val hospitalInTs  = dateTimeToMinutes(year, month, day, 15, 0 + float)
                val hospitalOutTs = dateTimeToMinutes(year, month, day, 16, 0 + float)
                items.add(GeofenceItem(timestamp = hospitalInTs,  lat = HOSPITAL_LAT, lng = HOSPITAL_LNG, status = GeofenceItem.STATUS_IN))
                items.add(GeofenceItem(timestamp = hospitalOutTs, lat = HOSPITAL_LAT, lng = HOSPITAL_LNG, status = GeofenceItem.STATUS_OUT))
            }

            // 场景4：傍晚5点半去老年活动中心，6点半回家
            if (dayOffset % 3 == 0) {
                val centerInTs  = dateTimeToMinutes(year, month, day, 17, 30 + float)
                val centerOutTs = dateTimeToMinutes(year, month, day, 18, 30 + float)
                items.add(GeofenceItem(timestamp = centerInTs,  lat = CENTER_LAT, lng = CENTER_LNG, status = GeofenceItem.STATUS_IN))
                items.add(GeofenceItem(timestamp = centerOutTs, lat = CENTER_LAT, lng = CENTER_LNG, status = GeofenceItem.STATUS_OUT))
            }
        }
        items
    }

    fun init(context: Context) {
        db = AppDatabase.getDatabase(context)
    }

    suspend fun insertBehaviorData() {
        Log.d(TAG, "insertBehaviorData: inserting ${behaviorEntities.size} records")
        val dao = db.dailyBehaviorDao()
        behaviorEntities.forEach { dao.insert(it) }
        Log.d(TAG, "insertBehaviorData: done")
    }

    suspend fun insertRiskData() {
        Log.d(TAG, "insertRiskData: inserting ${riskEntities.size} records")
        val riskDao = db.dailyRiskDao()
        riskEntities.forEach { riskDao.insert(it) }
        Log.d(TAG, "insertRiskData: done")
    }

    suspend fun insertGeofenceData() {
        Log.d(TAG, "insertGeofenceData: inserting ${geofenceEntities.size} records")
        val geofenceDao = db.geofenceItemDao()
        geofenceDao.insertAll(geofenceEntities)
        Log.d(TAG, "insertGeofenceData: done")
    }
}

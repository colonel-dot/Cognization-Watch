package mine.test

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.common.persistense.AppDatabase
import com.example.common.persistense.behavior.DailyBehaviorEntity
import com.example.common.persistense.risk.RiskLevel
import com.example.common.persistense.risk.DailyRiskEntity
import java.time.LocalDate

object DataInitializer {

    fun initializeTestData(context: Context) {
        // TODO: 假数据生成逻辑，已注释
        // CoroutineScope(Dispatchers.IO).launch {
        //     val database = AppDatabase.Companion.getDatabase(context)
        //     val behaviorDao = database.dailyBehaviorDao()
        //     val riskDao = database.dailyRiskDao()
        //
        //     val today = LocalDate.now()
        //
        //     // Insert DailyBehaviorEntity data for past 15 days with larger variations
        //     for (i in 0 until 15) {
        //         val date = today.minusDays(i.toLong())
        //
        //         // Create more varied data to produce larger risk score fluctuations
        //         // Using sine wave patterns to create natural-looking variations
        //         val sinValue = Math.sin(i * Math.PI / 7.0)
        //         val cosValue = Math.cos(i * Math.PI / 5.0)
        //
        //         // Wake time: varies between 5:00 AM (300) to 10:00 AM (600)
        //         val wakeMinute = 450 + (sinValue * 150).toInt()
        //         // Sleep time: varies between 9:00 PM (1260) to 1:00 AM (1500)
        //         val sleepMinute = 1380 + (cosValue * 120).toInt()
        //
        //         // Schulte test times: varies between good (15s) and poor (50s) performance
        //         val schulte16TimeSec = 20.0 + sinValue * 35.0
        //         val schulte25TimeSec = 35.0 + cosValue * 35.0
        //
        //         // Speech score: varies between 0.5 and 1.0
        //         val speechScore = 0.8 + sinValue * 0.5
        //
        //         // Steps: varies between 2000 and 16000
        //         val steps = 9000 + (sinValue * 7000).toInt()
        //
        //         // Active time: varies between 1200s (20min) and 9000s (2.5 hours)
        //         val activeTime = 4500 + (cosValue * 3900).toInt()
        //
        //         // Rest time: varies between 5-10 hours
        //         val restTime = 28800 + (sinValue * 9000).toInt()
        //
        //         val behaviorEntity = DailyBehaviorEntity(
        //             date = date,
        //             wakeMinute = wakeMinute,
        //             sleepMinute = sleepMinute,
        //             schulte16TimeSec = schulte16TimeSec,
        //             schulte25TimeSec = schulte25TimeSec,
        //             speechScore = speechScore,
        //             steps = steps,
        //             activeTime = activeTime,
        //             restTime = restTime
        //         )
        //
        //         // Insert or replace
        //         behaviorDao.insert(behaviorEntity)
        //
        //         // Create corresponding risk data
        //         // Predefined risk scores for larger variations (0.15 to 0.9)
        //         val targetRiskScores = listOf(
        //             0.15, 0.3, 0.6, 0.25, 0.7, 0.4, 0.8, 0.5, 0.85, 0.35, 0.75, 0.45, 0.65, 0.2, 0.9
        //         )
        //         val riskScore = targetRiskScores[i]
        //         val riskLevel = when {
        //             riskScore < 0.3 -> RiskLevel.认知情况正常
        //             riskScore < 0.6 -> RiskLevel.认知情况存在轻度风险
        //             else -> RiskLevel.认知能力有明显下滑趋势
        //         }
        //
        //         val riskEntity = DailyRiskEntity(
        //             date = date,
        //             riskScore = riskScore,
        //             riskLevel = riskLevel,
        //             sleepRisk = calculateSleepRisk(wakeMinute, sleepMinute),
        //             schulteRisk = calculateSchulteRisk(schulte16TimeSec, schulte25TimeSec),
        //             stepsRisk = calculateStepsRisk(steps, activeTime),
        //             speechRisk = calculateSpeechRisk(speechScore),
        //             alerted = false,
        //             explanations = generateExplanations(riskLevel, date)
        //         )
        //
        //         riskDao.insert(riskEntity)
        //     }
        // }
    }

    private fun calculateRiskScore(
        wakeMinute: Int, sleepMinute: Int, schulte16TimeSec: Double,
        schulte25TimeSec: Double, speechScore: Double, steps: Int,
        activeTime: Int, restTime: Int
    ): Double {
        // Simplified risk calculation
        val sleepRisk = calculateSleepRisk(wakeMinute, sleepMinute)
        val schulteRisk = calculateSchulteRisk(schulte16TimeSec, schulte25TimeSec)
        val stepsRisk = calculateStepsRisk(steps, activeTime)
        val speechRisk = calculateSpeechRisk(speechScore)

        return (sleepRisk + schulteRisk + stepsRisk + speechRisk) / 4.0
    }

    private fun calculateSleepRisk(wakeMinute: Int, sleepMinute: Int): Double {
        // Ideal sleep: wake between 6-8 AM (360-480), sleep between 10-11 PM (1320-1380)
        val wakeDeviation = when {
            wakeMinute < 360 -> (360 - wakeMinute) / 60.0 // too early
            wakeMinute > 480 -> (wakeMinute - 480) / 60.0 // too late
            else -> 0.0
        }
        val sleepDeviation = when {
            sleepMinute < 1320 -> (1320 - sleepMinute) / 60.0 // too early
            sleepMinute > 1380 -> (sleepMinute - 1380) / 60.0 // too late
            else -> 0.0
        }
        return (wakeDeviation + sleepDeviation) / 10.0 // normalize to 0-1 range
    }

    private fun calculateSchulteRisk(schulte16TimeSec: Double, schulte25TimeSec: Double): Double {
        // Ideal times: 16-grid < 25s, 25-grid < 40s
        val risk16 = maxOf(0.0, (schulte16TimeSec - 25.0) / 20.0)
        val risk25 = maxOf(0.0, (schulte25TimeSec - 40.0) / 30.0)
        return (risk16 + risk25) / 2.0
    }

    private fun calculateStepsRisk(steps: Int, activeTime: Int): Double {
        // Ideal: steps >= 10000, activeTime >= 3600s (1 hour)
        val stepsRisk = maxOf(0.0, (10000 - steps) / 10000.0)
        val activeRisk = maxOf(0.0, (3600 - activeTime) / 3600.0)
        return (stepsRisk + activeRisk) / 2.0
    }

    private fun calculateSpeechRisk(speechScore: Double): Double {
        // Ideal: speechScore >= 0.9
        return maxOf(0.0, (0.9 - speechScore) / 0.9)
    }

    private fun generateExplanations(riskLevel: RiskLevel, date: LocalDate): String {
        return when (riskLevel) {
            RiskLevel.认知情况正常 -> "所有指标均在正常范围内，继续保持良好习惯。"
            RiskLevel.认知情况存在轻度风险 -> "部分指标轻微偏离正常范围，建议关注作息规律和认知训练。"
            RiskLevel.认知能力有明显下滑趋势 -> "多个指标显示认知能力有所下降，建议增加认知训练并咨询专业医生。"
            else -> "数据不足无法评估。"
        }
    }
}
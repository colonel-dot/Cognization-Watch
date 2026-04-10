package risk.work

import android.util.Log
import risk.anomaly.AnomalyEngine
import risk.baseline.BaselineBuilder
import risk.fusion.RiskFusion
import risk.model.DailyRiskResult
import risk.model.NormalizedDailyBehavior
import com.example.common.persistense.risk.RiskLevel
import risk.trend.TrendDetector
import schulte.data.SchulteEvaluatorType
import java.time.LocalDate

private const val TAG = "DailyRiskCalculator"
object DailyRiskCalculator {

    fun calculate(data: List<NormalizedDailyBehavior>, evaluatorType: SchulteEvaluatorType): DailyRiskResult {
        
        val dataSize = data.size
        Log.d(TAG, "这个data的大小是${dataSize},最后一天的日期是${data[0].date}")

        if (dataSize < 4) {
            Log.d(TAG, "calculate: 数据不足，无法评估风险，正常表现3天以上以查看风险评估")
            return DailyRiskResult(
                date = LocalDate.now().minusDays(1),
                readRiskScore = 0.0,
                scheduleRiskScore = 0.0,
                schulteRiskScore = 0.0,
                stepsRiskScore = 0.0,
                riskScore = 0.0,
                riskLevel = RiskLevel.数据不足无法评估,
                explanations = listOf("数据不足，无法评估风险，正常表现3天以上以查看风险评估")
            )
        }

        val latest = data[dataSize - 2] // -1 是当天，取 -2 作为最新完整数据

        val explanations = mutableListOf<String>()

        // 语音
        val speechValues = data.mapNotNull { it.speechScore }
        val speechBaseline = BaselineBuilder.build(speechValues)
        val speechRisk = if (latest.speechScore != null && speechBaseline != null) {
            val zRisk = AnomalyEngine.zScoreAnomaly(latest.speechScore, speechBaseline)
            val trendRisk = TrendDetector.speechTrendRisk(speechValues)
            if (zRisk > 0.5 || trendRisk > 0.5)
                explanations.add("近期语音能力评分出现下降趋势")
            maxOf(zRisk, trendRisk)
        } else {
            explanations.add("语音能力没有明显下滑")
            0.0
        }

        // 舒尔特 优先25格，其次16格
        val schulteValues =
            data.mapNotNull { it.schulte25Time ?: it.schulte16Time }

     /* 后续对16格和25格算法分别处理
        val schulteValues = when (evaluatorType) {
            SchulteEvaluatorType.GRID_4 -> data.mapNotNull { it.schulte16Time }
            SchulteEvaluatorType.GRID_5 -> data.mapNotNull { it.schulte25Time  }
            else -> emptyList()
        } */

        val schulteBaseline = BaselineBuilder.build(schulteValues)
        val schulteRisk = if (schulteBaseline != null) {
            val zRisk = AnomalyEngine.zScoreAnomaly(
                schulteValues.last(),
                schulteBaseline
            )
            val trendRisk = TrendDetector.schulteTrendRisk(schulteValues)
            if (zRisk > 0.5 || trendRisk > 0.5)
                explanations.add("舒尔特方格完成时间变慢")
            maxOf(zRisk, trendRisk)
        } else {
            explanations.add("舒尔特方格的完成情况没有明显体现出认知情况的下滑")
            0.0
        }

        // 步数
        val stepValues = data.mapNotNull { it.steps?.toDouble() }
        val stepBaseline = BaselineBuilder.build(stepValues)
        val stepsRisk = if (latest.steps != null && stepBaseline != null) {
            val risk = AnomalyEngine.iqrAnomaly(latest.steps.toDouble(), stepBaseline)
            if (risk > 0.5) explanations.add("近期活动量明显减少")
            risk
        } else {
            explanations.add("活动量没有明显减少")
            0.0
        }

        // 作息
        val sleepValues = data.mapNotNull { it.sleepMinute?.toDouble() }
        val rhythmRisk = AnomalyEngine.rhythmAnomaly(sleepValues)
        if (rhythmRisk > 0.5) {
            explanations.add("作息时间波动明显")
        }
        else if (rhythmRisk > 0.0) {
            explanations.add("作息时间有轻微波动")
        }
        else {
            explanations.add("作息时间没有明显波动")
        }

        return RiskFusion.fuse(
            date = latest.date,
            sleepRisk = rhythmRisk,
            schulteRisk = schulteRisk,
            stepsRisk = stepsRisk,
            speechRisk = speechRisk,
            explanations = explanations
        )
    }
}
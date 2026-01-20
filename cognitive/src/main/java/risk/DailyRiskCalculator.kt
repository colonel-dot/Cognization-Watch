package risk

import risk.anomaly.AnomalyEngine
import risk.baseline.BaselineBuilder
import risk.fusion.RiskFusion
import risk.model.*
import risk.trend.TrendDetector

object DailyRiskCalculator {

    fun calculate(data: List<NormalizedDailyBehavior>): DailyRiskResult {

        val dataSize = data.size
        if (dataSize < 2) return DailyRiskResult(
            date = data.last().date.minusDays(1),
            readRiskScore = 0.0,
            scheduleRiskScore = 0.0,
            schulteRiskScore = 0.0,
            stepsRiskScore = 0.0,
            riskScore = 0.0,
            riskLevel = RiskLevel.NORMAL,
            explanations = listOf("数据不足，无法评估风险")
        )

        val latest = data[dataSize - 2] // 倒数第1天是当天，取倒数第2天作为最新完整数据

        val explanations = mutableListOf<String>()

        /** ===== 语音 ===== */
        val speechValues = data.mapNotNull { it.speechScore }
        val speechBaseline = BaselineBuilder.build(speechValues)
        val speechRisk = if (latest.speechScore != null && speechBaseline != null) {
            val zRisk = AnomalyEngine.zScoreAnomaly(latest.speechScore, speechBaseline)
            val trendRisk = TrendDetector.speechTrendRisk(speechValues)
            if (zRisk > 0.5 || trendRisk > 0.5)
                explanations.add("近期朗读评分出现下降趋势")
            maxOf(zRisk, trendRisk)
        } else 0.0

        /** ===== 舒尔特（优先25格，其次16格） ===== */
        val schulteValues =
            data.mapNotNull { it.schulte25Time ?: it.schulte16Time }

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
        } else 0.0

        /** ===== 步数 ===== */
        val stepValues = data.mapNotNull { it.steps?.toDouble() }
        val stepBaseline = BaselineBuilder.build(stepValues)
        val stepsRisk = if (latest.steps != null && stepBaseline != null) {
            val risk = AnomalyEngine.iqrAnomaly(latest.steps.toDouble(), stepBaseline)
            if (risk > 0.5) explanations.add("近期活动量明显减少")
            risk
        } else 0.0

        /** ===== 作息 ===== */
        val sleepValues = data.mapNotNull { it.sleepMinute?.toDouble() }
        val rhythmRisk = AnomalyEngine.rhythmAnomaly(sleepValues)
        if (rhythmRisk > 0.5) explanations.add("作息时间波动明显")

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

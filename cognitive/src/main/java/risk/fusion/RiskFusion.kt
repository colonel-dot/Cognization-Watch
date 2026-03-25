package risk.fusion

import com.example.common.persistense.risk.RiskLevel
import risk.model.*
import java.time.LocalDate

object RiskFusion {

    private val weights = mapOf(
        "sleep" to 0.25,
        "schulte" to 0.30,
        "steps" to 0.15,
        "speech" to 0.30
    )

    fun fuse(
        date: LocalDate,
        sleepRisk: Double,
        schulteRisk: Double,
        stepsRisk: Double,
        speechRisk: Double,
        explanations: List<String>
    ): DailyRiskResult {

        val score =
            sleepRisk * weights["sleep"]!! +
                    schulteRisk * weights["schulte"]!! +
                    stepsRisk * weights["steps"]!! +
                    speechRisk * weights["speech"]!!

        val level = when {
            score < 0.3 -> RiskLevel.认知情况正常
            score < 0.6 -> RiskLevel.认知情况存在轻度风险
            else -> RiskLevel.认知能力有明显下滑趋势
        }

        return DailyRiskResult(
            date = date,
            riskScore = score,
            readRiskScore = speechRisk,
            scheduleRiskScore = sleepRisk,
            schulteRiskScore = schulteRisk,
            stepsRiskScore = stepsRisk,
            riskLevel = level,
            explanations = explanations
        )
    }
}

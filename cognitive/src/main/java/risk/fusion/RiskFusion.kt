package risk.fusion

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
            score < 0.3 -> RiskLevel.NORMAL
            score < 0.6 -> RiskLevel.WARNING
            else -> RiskLevel.HIGH_RISK
        }

        return DailyRiskResult(
            date = date,
            riskScore = score,
            riskLevel = level,
            explanations = explanations
        )
    }
}

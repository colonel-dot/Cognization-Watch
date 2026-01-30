package risk.model

import risk.persistence.DailyRiskEntity
import java.time.LocalDate

data class DailyRiskResult(
    val date: LocalDate,
    val readRiskScore: Double,
    val scheduleRiskScore: Double,
    val schulteRiskScore: Double,
    val stepsRiskScore: Double,
    val riskScore: Double,
    val riskLevel: RiskLevel,
    val explanations: List<String>
)

// 扩展函数：业务模型 → 数据库实体
fun DailyRiskResult.toEntity(alerted: Boolean = false): DailyRiskEntity {
    return DailyRiskEntity(
        date = this.date,
        riskScore = this.riskScore,
        riskLevel = this.riskLevel,

        sleepRisk = this.scheduleRiskScore,
        schulteRisk = this.schulteRiskScore,
        stepsRisk = this.stepsRiskScore,
        speechRisk = this.readRiskScore,
        alerted = alerted,

        explanations = this.explanations.joinToString(separator = ";")
    )
}

fun DailyRiskEntity.toResult(): DailyRiskResult {
    return DailyRiskResult(
        date = this.date,
        readRiskScore = this.speechRisk,
        scheduleRiskScore = this.sleepRisk,
        schulteRiskScore = this.schulteRisk,
        stepsRiskScore = this.stepsRisk,
        riskScore = this.riskScore,
        riskLevel = this.riskLevel,
        explanations = this.explanations.split(";").filter { it.isNotBlank() }
    )
}


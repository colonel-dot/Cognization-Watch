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
        // 业务字段映射到数据库字段（按实际语义对应）
        sleepRisk = this.scheduleRiskScore,
        schulteRisk = this.schulteRiskScore,
        stepsRisk = this.stepsRiskScore,
        speechRisk = this.readRiskScore,
        alerted = alerted,
        // 集合转字符串存储（如分号分隔，也可用Gson转JSON）
        explanations = this.explanations.joinToString(separator = ";")
    )
}

// 扩展函数：数据库实体 → 业务模型
fun DailyRiskEntity.toResult(): DailyRiskResult {
    return DailyRiskResult(
        date = this.date,
        readRiskScore = this.speechRisk,
        scheduleRiskScore = this.sleepRisk,
        schulteRiskScore = this.schulteRisk,
        stepsRiskScore = this.stepsRisk,
        riskScore = this.riskScore,
        riskLevel = this.riskLevel,
        // 字符串转回集合
        explanations = this.explanations.split(";").filter { it.isNotBlank() }
    )
}


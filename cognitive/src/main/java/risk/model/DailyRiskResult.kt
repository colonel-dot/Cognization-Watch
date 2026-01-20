package risk.model

import java.time.LocalDate

data class DailyRiskResult(
    val date: LocalDate,
    val riskScore: Double,
    val riskLevel: RiskLevel,
    val explanations: List<String>
)

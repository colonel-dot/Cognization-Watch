package risk.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import risk.model.RiskLevel

@Entity(tableName = "daily_risk")
data class DailyRiskEntity(
    @PrimaryKey val date: LocalDate,

    val riskScore: Double,//风险分数
    val riskLevel: RiskLevel,//总的风险等级

    val sleepRisk: Double,
    val schulteRisk: Double,
    val stepsRisk: Double,
    val speechRisk: Double,

    val alerted: Boolean = false,

    val explanations: String
)

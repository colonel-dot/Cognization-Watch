package persistense.risk

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_risk")
data class DailyRiskEntity(
    @PrimaryKey val date: LocalDate,

    val riskScore: Double,//风险指数
    val riskLevel: RiskLevel = RiskLevel.数据不足无法评估,//总的风险等级

    val sleepRisk: Double = 0.0,
    val schulteRisk: Double = 0.0,
    val stepsRisk: Double = 0.0,
    val speechRisk: Double = 0.0,

    val alerted: Boolean = false,

    val explanations: String = ""
)

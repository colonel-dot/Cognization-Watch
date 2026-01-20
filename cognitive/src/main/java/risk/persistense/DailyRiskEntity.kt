package risk.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import risk.model.RiskLevel

@Entity(tableName = "daily_risk")
data class DailyRiskEntity(
    @PrimaryKey val date: LocalDate,

    val riskScore: Double,
    val riskLevel: RiskLevel,

    /** 子风险（方便解释 & 画图） */
    val sleepRisk: Double,
    val schulteRisk: Double,
    val stepsRisk: Double,
    val speechRisk: Double,

    /** 是否触发告警 */
    val alerted: Boolean = false,

    /** 解释文本（JSON / 分号分隔都行） */
    val explanations: String
)

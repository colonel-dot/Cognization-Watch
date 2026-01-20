package risk.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.LocalDate

@Dao
interface DailyRiskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(risk: DailyRiskEntity)

    @Query("SELECT * FROM daily_risk WHERE date = :date")
    suspend fun getByDate(date: LocalDate): DailyRiskEntity?

    @Query("SELECT * FROM daily_risk ORDER BY date ASC")
    suspend fun getAll(): List<DailyRiskEntity>

    @Query("""
        SELECT * FROM daily_risk
        WHERE date >= :from
        ORDER BY date ASC
    """)
    suspend fun loadFrom(from: LocalDate): List<DailyRiskEntity>

    @Query("""
        SELECT * FROM daily_risk
        WHERE riskLevel = 'HIGH_RISK'
        AND alerted = 0
    """)
    suspend fun loadUnalertedHighRisk(): List<DailyRiskEntity>
}

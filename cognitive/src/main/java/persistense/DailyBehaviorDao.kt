package persistense

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.time.LocalDate

@Dao
interface DailyBehaviorDao {

    @Transaction // 事务保证原子性，查和插是一个整体，不会并发出错
    suspend fun getOrInitTodayBehavior(date: LocalDate): DailyBehaviorEntity {
        // 1. 查询当天是否有数据
        val existEntity = getByDate(date)
        return if (existEntity != null) {
            // 2. 有数据 → 直接返回
            existEntity
        } else {
            // 3. 无数据 → 初始化一条默认值的空数据并插入
            val newEntity = DailyBehaviorEntity(
                date = date,
                wakeMinute = 0,
                sleepMinute = 0,
                schulte16TimeSec = 0.0,
                schulte25TimeSec = 0.0,
                speechScore = 0.0,
                steps = 0
            )
            insert(newEntity)
            // 插入后返回这条新数据
            newEntity
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotExists(entity: DailyBehaviorEntity)


    @Insert
    suspend fun insert(dailyBehaviorEntity: DailyBehaviorEntity)

    @Delete
    suspend fun delete(dailyBehaviorEntity: DailyBehaviorEntity)

    @Update
    suspend fun update(dailyBehaviorEntity: DailyBehaviorEntity)

    @Query("SELECT * FROM daily_behavior")
    suspend fun getAll(): List<DailyBehaviorEntity>

    @Query("SELECT * FROM daily_behavior WHERE date = :date")
    suspend fun getByDate(date: LocalDate): DailyBehaviorEntity?

    @Query("""
        UPDATE daily_behavior
        SET speechScore = :score
        WHERE date = :date
    """)
    suspend fun updateSpeechScore(
        date: LocalDate,
        score: Double
    )

    @Query("""
        UPDATE daily_behavior
        SET schulte16TimeSec = :time
        WHERE date = :date
    """)
    suspend fun updateSchulte16Time(
        date: LocalDate,
        time: Double
    )

    @Query("""
        UPDATE daily_behavior
        SET schulte25TimeSec = :time
        WHERE date = :date
    """)
    suspend fun updateSchulte25Time(
        date: LocalDate,
        time: Double
    )

    @Query("""
    UPDATE daily_behavior
    SET wakeMinute = :wakeMinute,
        sleepMinute = :sleepMinute
    WHERE date = :date
""")
    suspend fun updateSchedule(
        date: LocalDate,
        wakeMinute: Int,
        sleepMinute: Int
    )


    @Query("""
        UPDATE daily_behavior
        SET steps = :steps
        WHERE date = :date
    """)
    suspend fun updateSteps(
        date: LocalDate,
        steps: Int
    )

    @Query("""
        UPDATE daily_behavior
        SET steps = :steps,
            activeTime = :activeTime,
            restTime = :restTime
        WHERE date = :date
    """)
    suspend fun updateSports(
        date: LocalDate,
        steps: Int,
        activeTime: Int,
        restTime: Int
    )

    @Query("SELECT * FROM daily_behavior WHERE date >= :date ORDER BY date ASC")
    suspend fun loadFrom(date: LocalDate): List<DailyBehaviorEntity>


    @Query("SELECT * FROM daily_behavior WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    suspend fun loadRange(start: LocalDate, end: LocalDate): List<DailyBehaviorEntity>

    @Query("""
    SELECT * FROM daily_behavior
    WHERE date < :date
    ORDER BY date DESC
    LIMIT 15
""")
    suspend fun loadPrev15Days(date: LocalDate): List<DailyBehaviorEntity>

}
package persistense

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DailyBehaviorDao {
    @Insert
    suspend fun insert(dailyBehaviorEntity: DailyBehaviorEntity)

    @Delete
    suspend fun delete(dailyBehaviorEntity: DailyBehaviorEntity)

    @Update
    suspend fun update(dailyBehaviorEntity: DailyBehaviorEntity)

    @Query("SELECT * FROM daily_behavior WHERE date = :date")
    suspend fun getByDate(date: String): DailyBehaviorEntity?
}
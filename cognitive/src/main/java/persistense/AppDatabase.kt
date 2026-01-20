package persistense

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.TypeConverters
import risk.persistence.DailyRiskDao
import risk.persistence.DailyRiskEntity

@TypeConverters(LocalDateConverter::class)
@Database(entities = [DailyBehaviorEntity::class, DailyRiskEntity::class], version = 1)
abstract class AppDatabase : androidx.room.RoomDatabase() {
    abstract fun dailyBehaviorDao(): DailyBehaviorDao
    abstract fun dailyRiskDao(): DailyRiskDao
    companion object {
        private var instance : AppDatabase ?= null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(context.applicationContext,
                AppDatabase::class.java, "daily_behavior_database")
                .build().apply { instance = this }
        }
    }
}
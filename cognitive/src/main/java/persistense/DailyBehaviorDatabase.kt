package persistense

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.TypeConverters

@TypeConverters(LocalDateConverter::class)
@Database(entities = [DailyBehaviorEntity::class], version = 1)
abstract class DailyBehaviorDatabase : androidx.room.RoomDatabase() {
    abstract fun dailyBehaviorDao(): DailyBehaviorDao

    companion object {
        private var instance : DailyBehaviorDatabase ?= null

        @Synchronized
        fun getDatabase(context: Context): DailyBehaviorDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(context.applicationContext,
                DailyBehaviorDatabase::class.java, "daily_behavior_database").build().apply { instance = this }
        }
    }
}
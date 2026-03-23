package mine.data

import android.app.Application
import persistense.AppDatabase
import persistense.behavior.DailyBehaviorEntity
import java.time.LocalDate

class MineRecordModel(application: Application) {
    private val behaviorDao = AppDatabase
        .getDatabase(application)
        .dailyBehaviorDao()

    suspend fun queryRecordDataByDate(date: LocalDate): DailyBehaviorEntity? {
        return behaviorDao.getByDate(date)
    }

    suspend fun queryAllRecords(): List<DailyBehaviorEntity> {
        return behaviorDao.getAll()
    }
}
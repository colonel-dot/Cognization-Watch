package mine.data

import android.app.Application
import java.time.LocalDate

class MineRecordModel(application: Application) {
    private val behaviorDao = persistense.AppDatabase
        .getDatabase(application)
        .dailyBehaviorDao()

    suspend fun queryRecordDataByDate(date: LocalDate): persistense.DailyBehaviorEntity? {
        return behaviorDao.getByDate(date)
    }

    suspend fun queryAllRecords(): List<persistense.DailyBehaviorEntity> {
        return behaviorDao.getAll()
    }
}
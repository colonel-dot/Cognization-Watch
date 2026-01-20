package mine.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class MineRecordModel(application: Application) {
    private val behaviorDao = persistense.DailyBehaviorDatabase
        .getDatabase(application)
        .dailyBehaviorDao()

    // 核心：查询当日所有数据的方法，外部调用这个方法即可
    suspend fun queryRecordDataByDate(date: LocalDate): persistense.DailyBehaviorEntity? {
        return behaviorDao.getByDate(date)
    }

    suspend fun queryAllRecords(): List<persistense.DailyBehaviorEntity> {
        return behaviorDao.getAll()
    }
}
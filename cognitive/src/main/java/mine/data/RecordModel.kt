package mine.data

import android.app.Application
import com.example.common.persistense.AppDatabase
import com.example.common.persistense.risk.DailyRiskDao
import com.example.common.persistense.risk.DailyRiskEntity
import java.time.LocalDate

class RecordModel(application: Application) {
    private val riskDao: DailyRiskDao = AppDatabase
        .getDatabase(application)
        .dailyRiskDao()

    suspend fun queryRiskByDate(date: LocalDate): DailyRiskEntity? {
        return riskDao.getByDate(date)
    }

    suspend fun queryAllRiskRecords(): List<DailyRiskEntity> {
        return riskDao.getAll()
    }

    suspend fun queryRiskRecords(fromDate: LocalDate): List<DailyRiskEntity> {
        return riskDao.loadFrom(fromDate)
    }
}

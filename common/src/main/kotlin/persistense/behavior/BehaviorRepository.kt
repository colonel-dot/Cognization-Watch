package persistense.behavior

import kotlinx.coroutines.runBlocking
import java.time.LocalDate

object BehaviorRepository {

    fun getByDateBlocking(dao: DailyBehaviorDao, date: LocalDate): DailyBehaviorEntity? {
        return runBlocking {
            dao.getByDate(date)
        }
    }
}


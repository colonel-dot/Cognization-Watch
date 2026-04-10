package com.example.common.persistense.behavior

import kotlinx.coroutines.runBlocking
import java.time.LocalDate

object BehaviorRepository {

    // TODO
    fun getByDateBlocking(dao: DailyBehaviorDao, date: LocalDate): DailyBehaviorEntity? {
        return runBlocking {
            dao.getByDate(date)
        }
    }
}


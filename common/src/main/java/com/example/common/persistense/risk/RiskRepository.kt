package com.example.common.persistense.risk

import kotlinx.coroutines.runBlocking
import java.time.LocalDate

object RiskRepository {

    fun getAllBlocking(dao: DailyRiskDao): List<DailyRiskEntity> {
        return runBlocking {
            dao.getAll()
        }
    }

    fun getFromBlocking(dao: DailyRiskDao, from: LocalDate): List<DailyRiskEntity> {
        return runBlocking {
            dao.loadFrom(from)
        }
    }

    fun getByDateBlocking(dao: DailyRiskDao, date: LocalDate): DailyRiskEntity? {
        return runBlocking {
            dao.getByDate(date)
        }
    }
}
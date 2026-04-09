package com.example.common.repository.network

import android.util.Log
import com.example.common.persistense.behavior.DailyBehaviorEntity
import com.example.common.persistense.risk.DailyRiskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "DashboardNetWorkRepo"
private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

object DashboardNetworkRepository {
    private val dashboardApiService by lazy {
        RetrofitClient.createService(DashboardApiService::class.java)
    }

    private fun <T> wrapRequest(
        errorMsg: String,
        request: suspend () -> T
    ): Flow<Result<T>> = flow {
        try {
            emit(Result.success(request()))
        } catch (e: Exception) {
            Log.e(TAG, "$errorMsg: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getElderDailyBehavior(
        childAccount: String,
        elderAccount: String,
        date: LocalDate
    ): Flow<Result<DailyBehaviorEntity>> =
        wrapRequest("获取老人行为数据失败") {
            dashboardApiService.getDailyBehavior(childAccount, elderAccount, date.format(DATE_FORMATTER))
        }

    fun getElderDailyRisk(
        childAccount: String,
        elderAccount: String,
        date: LocalDate
    ): Flow<Result<DailyRiskEntity>> =
        wrapRequest("获取老人风险数据失败") {
            dashboardApiService.getDailyRisk(childAccount, elderAccount, date.format(DATE_FORMATTER))
        }
}

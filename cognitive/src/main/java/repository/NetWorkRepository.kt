package repository

import debug_login.LoginRequest
import debug_login.LoginResponse
import debug_login.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import persistense.DailyBehaviorEntity
import risk.model.DailyRiskResult
import risk.model.toResult
import risk.persistence.DailyRiskEntity
import java.time.LocalDate

object NetWorkRepository {
    fun updateDailyBehavior(account: String, date: LocalDate, dailyBehaviorEntity: DailyBehaviorEntity): Flow<Result<Int>> = flow {

        try {
            RetrofitClient.apiService.postDailyBehavior(account, date, dailyBehaviorEntity)
            // 发送成功结果
            emit(Result.success(200))
        } catch (e: Exception) {
            // 捕获所有异常，发送失败结果
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun updateDailyRisk(account: String, date: LocalDate, riskEntity: DailyRiskResult): Flow<Result<Int>> = flow {
        try {
            RetrofitClient.apiService.postDailyRisk(account, date, riskEntity)
            // 发送成功结果
            emit(Result.success(200))
        } catch (e: Exception) {
            // 捕获所有异常，发送失败结果
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getOtherDailyRisk(account: String?, date: LocalDate): Flow<Result<DailyRiskResult>> = flow {
        try {
            if (account == null) {
                throw IllegalArgumentException("Account cannot be null")
            }
            val response = RetrofitClient.apiService.getDailyRisk(account, date)
            // 发送成功结果
            emit(Result.success(response.toResult()))
        } catch (e: Exception) {
            // 捕获所有异常，发送失败结果
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getOtherDailyBehavior(account: String?, date: LocalDate): Flow<Result<DailyBehaviorEntity>> = flow {
        try {
            if (account == null) {
                throw IllegalArgumentException("Account cannot be null")
            }
            val response = RetrofitClient.apiService.getDailyBehavior(account, date)
            // 发送成功结果
            emit(Result.success(response))
        } catch (e: Exception) {
            // 捕获所有异常，发送失败结果
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

}
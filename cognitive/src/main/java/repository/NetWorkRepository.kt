package repository

import android.util.Log
import debug_login.ApiService
import debug_login.LoginRequest
import debug_login.LoginResponse
import Internet.RetrofitClient
import debug_login.UpdateDailyHealthRequest
import debug_login.UpdateDailyRiskRequest
import geobairrer.BarrierInfo
import geobairrer.ElderMovement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import persistense.DailyBehaviorEntity
import risk.model.DailyRiskResult
import risk.model.RiskLevel
import risk.model.toResult
import risk.persistence.DailyRiskEntity
import user.UserManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "NetWorkRepository"
val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE // 格式：yyyy-MM-dd

object NetWorkRepository {
    fun updateDailyBehavior(account: String, date: LocalDate, record: DailyBehaviorEntity): Flow<Result<Int>> = flow {
        try {
            val dateStr = date.toString()

            // 将实体转换为 Map
            val dataMap = mapOf(
                "wake_time" to record.wakeMinute,
                "sleep_time" to record.sleepMinute,
                "schulte4" to record.schulte16TimeSec,
                "schulte5" to record.schulte25TimeSec,
                "voice_score" to record.speechScore,
                "step_count" to record.steps
            )

            Log.d(TAG, "updateDailyBehavior: 将给后端post的日常数据是 $dateStr $dataMap ${dataMap["wake_time"]} ${dataMap["voice_score"]}")

            val request = UpdateDailyHealthRequest(
                elder_account = account,
                date = dateStr,
                data = dataMap
            )

            val response = RetrofitClient.createService(ApiService::class.java).postDailyBehavior(request)
            if (response.isSuccessful) {
                emit(Result.success(response.code()))
            } else {
                // 处理非成功响应
                val errorMsg = "健康数据更新失败: ${response.code()} - ${response.errorBody()?.use { it.string() }}"
                Log.e(TAG, errorMsg)
                emit(Result.failure(IllegalStateException(errorMsg)))
            }
        } catch (e: Exception) {
            Log.e("NetWorkRepository", "健康数据更新异常: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun updateDailyRisk(account: String, date: LocalDate, riskResult: DailyRiskResult): Flow<Result<Int>> = flow {
        try {
            val dateStr = date.toString()

            // 将风险结果转换为 Map
            val dataMap = mutableMapOf<String, Any?>().apply {
                put("overallRiskLevel", riskResult.riskLevel)
                put("overallRiskScore", riskResult.riskScore)
                put("schulteRisk", riskResult.schulteRiskScore)
                put("speechRisk", riskResult.readRiskScore)
                put("sleepRisk", riskResult.scheduleRiskScore)
                // 如果有其他字段也一并添加
            }

            val request = UpdateDailyRiskRequest(
                elder_account = account,
                date = dateStr,
                data = dataMap
            )

            Log.d(TAG, "发送风险数据: account=$account, date=$dateStr, data=$dataMap")

            val response = RetrofitClient.createService(ApiService::class.java).postDailyRisk(request)
            if (response.isSuccessful) {
                Log.d(TAG, "数据更新成功，响应码: ${response.code()}")
                emit(Result.success(response.code()))
            } else {
                // 处理非成功响应
                val errorMsg = "数据更新失败: ${response.code()} - ${response.errorBody()?.use { it.string() }}"
                Log.e(TAG, errorMsg)
                emit(Result.failure(IllegalStateException(errorMsg)))
            }
            /*if (response.isSuccessful) {
                Log.d(TAG, "风险数据更新成功")
            } else {
                Log.e(TAG, " 风险数据更新失败: ${response.code()} - ${response.errorBody()?.string()}")
            }*/
        }catch (e: Exception) {
            Log.e(TAG, " 风险数据更新异常: ${e.message}", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getOtherDailyRisk(otheraccount: String?, date: LocalDate): Flow<Result<DailyRiskResult>> = flow {
        try {
            if (otheraccount == null) {
                throw IllegalArgumentException("Account cannot be null")
            }
            val response = RetrofitClient.createService(ApiService::class.java).getDailyRisk(UserManager.getUserId(), otheraccount, date.format(DATE_FORMATTER))
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
            val response = RetrofitClient.createService(ApiService::class.java).getDailyBehavior(UserManager.getUserId(), account, date.format(DATE_FORMATTER))
            // 发送成功结果
            emit(Result.success(response))
        } catch (e: Exception) {
            // 捕获所有异常，发送失败结果
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getBarrierInfo(): Flow<Result<BarrierInfo>> = flow {
        TODO()
    }

    fun postBarrierInfo(barrierInfo: BarrierInfo): Flow<Result<Int>> = flow {
        TODO()

    }

    fun getElderMovement(): Flow<Result<ElderMovement>> = flow {
        TODO()
    }

     fun postElderMovement(elderMovement: ElderMovement): Flow<Result<Int>> {
        TODO()
    }

}
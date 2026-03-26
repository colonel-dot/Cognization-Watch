package bind_device

import android.util.Log
import network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import com.example.common.persistense.behavior.DailyBehaviorEntity
import com.example.common.persistense.risk.DailyRiskEntity
import debug_login.ApiService

private const val TAG = "BindRepository"

object BindRepository {

    fun bind(request: BindRequest): Flow<Result<BindResponse>> = flow{
        try {
            val request = BindRequest(request.musername, request.otherusername)
            val response = RetrofitClient.createService(ApiService::class.java).bind(request)
            emit(Result.success(response))
        }
        catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getOtherAllBehavior(account: String): Flow<Result<List<DailyBehaviorEntity>>> = flow {
        try {
            val response = RetrofitClient.createService(ApiService::class.java).getAllDailyBehavior(account)
            Log.d(TAG, "getOtherAllBehavior: 成功得到对方所有行为数据，该数据是 $response")
            emit(Result.success(response))
        } catch (e: Exception) {
            Log.d(TAG, "getOtherAllBehavior: 得到对方所有行为数据出错")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getOtherAllRisk(account: String): Flow<Result<List<DailyRiskEntity>>> = flow {
        try {
            val response = RetrofitClient.createService(ApiService::class.java).getAllDailyRisk(account)
            Log.d(TAG, "getOtherAllRisk: 成功得到对方所有风险指数数据，该数据是 $response")
            emit(Result.success(response))
        } catch (e: Exception) {
            Log.d(TAG, "getOtherAllRisk: 得到对方所有风险数据出错")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

}
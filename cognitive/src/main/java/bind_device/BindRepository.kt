package bind_device

import debug_login.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import persistense.DailyBehaviorEntity
import risk.persistence.DailyRiskEntity

object BindRepository {

    fun bind(request: BindRequest): Flow<Result<BindResponse>> = flow{
        try {
            val request = BindRequest(request.musername, request.otherusername)
            val response = RetrofitClient.apiService.bind(request)
            emit(Result.success(response))
        }
        catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getOtherAllBehavior(account: String): Flow<Result<List<DailyBehaviorEntity>>> = flow {
        try {
            val response = RetrofitClient.apiService.getAllDailyBehavior(account)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getOtherAllRisk(account: String): Flow<Result<List<DailyRiskEntity>>> = flow {
        try {
            val response = RetrofitClient.apiService.getAllDailyRisk(account)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

}
package bind_device

import debug_login.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class BindRepository {
    suspend fun bindDevice(request: BindRequest): Flow<Result<BindResponse>> = flow{
        try {
            val request = BindRequest(request.musername, request.otherusername)
            val response = RetrofitClient.apiService.bind(request)
            emit(Result.success(response))
        }
        catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
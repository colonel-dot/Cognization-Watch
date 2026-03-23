package debug_login

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LoginRepository {
    // 登录请求封装为 Flow
    fun login(username: String, password: String): Flow<Result<LoginResponse>> = flow {
        try {
            // 发起网络请求（suspend 函数，Retrofit 原生支持，无需适配器）
            val request = LoginRequest(username, password)
            val response = RetrofitClient.createService(ApiService::class.java).login(request)
            // 发送成功结果
            emit(Result.success(response))
        } catch (e: Exception) {
            // 捕获所有异常，发送失败结果
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO) // 指定网络请求在 IO 线程执行
}
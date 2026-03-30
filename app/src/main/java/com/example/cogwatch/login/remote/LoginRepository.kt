package com.example.cogwatch.login.remote

import debug_login.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient
import com.example.common.login.remote.LoginRequest
import com.example.common.login.remote.LoginResponse

class LoginRepository {

    // 登录请求封装为 Flow
    fun login(username: String, password: String): Flow<Result<LoginResponse>> = flow<Result<LoginResponse>> {
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
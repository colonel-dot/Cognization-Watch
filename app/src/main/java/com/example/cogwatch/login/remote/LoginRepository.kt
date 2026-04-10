package com.example.cogwatch.login.remote

import remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import com.example.common.repository.network.RetrofitClient
import com.example.common.login.remote.LoginRequest

class LoginRepository {

    fun login(username: String, password: String) = flow {
        try {
            val request = LoginRequest(username, password)
            val response = RetrofitClient.createService(ApiService::class.java).login(request)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
package com.example.common.login.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}

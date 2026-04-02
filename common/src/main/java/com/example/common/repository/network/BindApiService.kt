package com.example.common.repository.network

import com.example.common.bind_device.BindRequest
import com.example.common.bind_device.BindResponse
import com.example.common.persistense.behavior.DailyBehaviorEntity
import com.example.common.persistense.risk.DailyRiskEntity
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Query

interface BindApiService {
    @POST("bind")
    suspend fun bind(@Body request: BindRequest): BindResponse

    // 2. 对应 /daily/all 接口（GetAllDailyHandler）
    @GET("daily/all")
    suspend fun getAllDailyBehavior(
        @Query("elder_account") account: String // 关键：account → elder_account
    ): List<DailyBehaviorEntity>

    // 4. 对应 /daily/allrisk 接口（GetAllDailyRiskHandler）
    @GET("daily/allrisk")
    suspend fun getAllDailyRisk(
        @Query("elder_account") account: String // 关键：account → elder_account
    ): List<DailyRiskEntity>
}
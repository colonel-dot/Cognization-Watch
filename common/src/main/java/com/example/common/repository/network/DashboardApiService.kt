package com.example.common.repository.network

import com.example.common.persistense.behavior.DailyBehaviorEntity
import com.example.common.persistense.risk.DailyRiskEntity
import retrofit2.http.GET
import retrofit2.http.Query

interface DashboardApiService {
    @GET("elder/daily")
    suspend fun getDailyBehavior(
        @Query("username") child_account: String?,
        @Query("elder_account") elder_account: String?,
        @Query("date") date: String
    ): DailyBehaviorEntity

    @GET("elder/dailyrisk")
    suspend fun getDailyRisk(
        @Query("username") childAccount: String?,
        @Query("elder_account") elderAccount: String?,
        @Query("date") date: String
    ): DailyRiskEntity
}
package com.example.bridge.network

import com.example.bridge.model.BarrierInfo
import com.example.bridge.model.ElderMovement
import com.example.bridge.network.GeoNetworkRepository.BarrierInfoRequest
import com.example.bridge.network.GeoNetworkRepository.ElderMovementRequest
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface GeoApiService {
    @GET("elder/barrierinfo")
    suspend fun getBarrierInfo(
        @Query("childname") youngname: String
    ): BarrierInfo

    @POST("elder/updatebarrierinfo")
    suspend fun postBarrierInfo(@Body barrierInfoRequest: BarrierInfoRequest): Int

    @GET("elder/eldermovement")
    suspend fun getElderMovement(
        @Query("eldername") eldername: String
    ): ElderMovement

    @POST("elder/updateeldermovement")
    suspend fun postElderMovement(@Body elderMovementRequest: ElderMovementRequest): Int
}
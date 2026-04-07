package com.example.common.geofence.network

import com.example.common.geofence.model.BarrierInfo
import com.example.common.geofence.model.ElderMovement
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
    suspend fun postBarrierInfo(@Body request: BarrierInfoRequest): Int

    @GET("elder/eldermovement")
    suspend fun getElderMovement(
        @Query("eldername") eldername: String
    ): ElderMovement

    @POST("elder/updateeldermovement")
    suspend fun postElderMovement(@Body request: ElderMovementRequest): Int
}

data class BarrierInfoRequest(
    val eldername: String,
    val data: Map<String, Any?>
)

data class ElderMovementRequest(
    val childname: String,
    val data: Map<String, Any?>
)
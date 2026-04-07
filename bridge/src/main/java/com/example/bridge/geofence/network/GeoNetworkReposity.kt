package com.example.bridge.geofence.network

import android.util.Log
import com.example.common.geofence.model.BarrierInfo
import com.example.common.geofence.model.ElderMovement
import com.example.common.geofence.network.GeoApiService
import com.example.common.geofence.network.BarrierInfoRequest
import com.example.common.geofence.network.ElderMovementRequest
import com.example.common.repository.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch


private const val TAG = "GeoNetWorkReposity"


object GeoNetworkRepository {

    private val geoApiService by lazy {
        RetrofitClient.createService(GeoApiService::class.java)
    }

    private fun <T> wrapRequest(
        errorMsg: String,
        request: suspend () -> T
    ): Flow<Result<T>> = flow {
        val result = request()
        emit(Result.success(result))
    }.catch { e ->
        Log.d(TAG, "$errorMsg: ${e.message}", e)
        emit(Result.failure(e))
    }

    private fun wrapPostRequest(
        errorMsg: String,
        request: suspend () -> Boolean
    ): Flow<Result<Unit>> = flow {
        val isSuccess = request()
        if (isSuccess) {
            emit(Result.success(Unit))
        } else {
            emit(Result.failure(IllegalStateException(errorMsg)))
        }
    }.catch { e ->
        Log.d(TAG, "$errorMsg: ${e.message}", e)
        emit(Result.failure(e))
    }

    fun getFenceInfo(childname: String): Flow<Result<BarrierInfo>> =
        wrapRequest("获取围栏信息失败") {
            geoApiService.getBarrierInfo(childname)
        }

    fun postBarrierInfo(eldername: String, barrier: BarrierInfo): Flow<Result<Unit>> =
        wrapPostRequest("围栏信息发送失败") {
            val dataMap = mapOf(
                "eldername" to barrier.eldername,
                "lon" to barrier.lon,
                "lat" to barrier.lat,
                "radius" to barrier.radius
            )
            val request = BarrierInfoRequest(eldername, dataMap)
            val responseCode = geoApiService.postBarrierInfo(request)
            responseCode == 200
        }

    fun getElderMovement(eldername: String): Flow<Result<ElderMovement>> =
        wrapRequest("获取老人轨迹失败") {
            geoApiService.getElderMovement(eldername)
        }

    fun postElderMovement(childname: String, elderMovement: ElderMovement): Flow<Result<Unit>> =
        wrapPostRequest("老人轨迹发送失败") {
            val dataMap = mapOf(
                "childname" to elderMovement.childname,
                "lon" to elderMovement.lon,
                "lat" to elderMovement.lat,
                "time" to elderMovement.time,
                "status" to elderMovement.status
            )
            val request = ElderMovementRequest(childname, dataMap)
            val responseCode = geoApiService.postElderMovement(request)
            responseCode == 200
        }
}
package geofence.network

import android.util.Log
import com.example.common.geofence.model.BarrierInfo
import com.example.common.geofence.model.ElderMovement
import com.example.common.geofence.network.ElderMovementRequest
import com.example.common.geofence.network.GeoApiService
import com.example.common.repository.network.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch

private const val TAG = "ElderMovementRepository"

object ElderMovementRepository {

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

    /**
     * 获取围栏配置信息（cognitive 端从服务器拉取）
     */
    fun getBarrierInfo(childname: String): Flow<Result<BarrierInfo>> =
        wrapRequest("获取围栏配置失败") {
            geoApiService.getBarrierInfo(childname)
        }

    /**
     * 上报老人轨迹事件（cognitive 端主动推送）
     */
    fun postElderMovement(childname: String, elderMovement: ElderMovement): Flow<Result<Unit>> =
        wrapPostRequest("轨迹上报失败") {
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
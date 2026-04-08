package geofence.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.fence.GeoFence
import com.amap.api.fence.GeoFenceListener
import com.example.common.geofence.model.BarrierInfo
import geofence.manager.CognitiveGeofenceManager
import geofence.network.ElderMovementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GeofenceInitState {
    object Idle : GeofenceInitState()
    object Loading : GeofenceInitState()
    data class Success(val barrierInfo: BarrierInfo) : GeofenceInitState()
    data class Error(val msg: String) : GeofenceInitState()
}

class CognitiveGeofenceViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CognitiveGeoViewModel"
    }

    private val geofenceManager = CognitiveGeofenceManager(application)

    private val _initState = MutableStateFlow<GeofenceInitState>(GeofenceInitState.Idle)
    val initState: StateFlow<GeofenceInitState> = _initState.asStateFlow()

    private var isInitialized = false

    init {
        // 设置围栏创建回调
        geofenceManager.setFenceListener(object : GeoFenceListener {
            override fun onGeoFenceCreateFinished(geoFenceList: MutableList<GeoFence>?, errorCode: Int, errorMsg: String?) {
                if (errorCode == 0 && !geoFenceList.isNullOrEmpty()) {
                    Log.d(TAG, "Geofence created successfully: ${geoFenceList[0].fenceId}")
                } else {
                    Log.e(TAG, "Geofence create failed: $errorCode - $errorMsg")
                }
            }
        })
    }

    /**
     * 拉取围栏配置并创建本地围栏
     * @param childname Bridge 设备的账号（作为围栏创建者的标识）
     */
    fun pullAndCreateGeofence(childname: String) {
        if (isInitialized) {
            Log.w(TAG, "Geofence already initialized, skip")
            return
        }

        _initState.value = GeofenceInitState.Loading

        viewModelScope.launch {
            ElderMovementRepository.getBarrierInfo(childname)
                .collect { result ->
                    result.onSuccess { barrierInfo ->
                        Log.d(TAG, "Pulled barrier info: $barrierInfo")
                        _initState.value = GeofenceInitState.Success(barrierInfo)
                        val created = geofenceManager.createGeofence(barrierInfo)
                        if (created) {
                            Log.d(TAG, "Local geofence created from barrier info")
                        } else {
                            Log.w(TAG, "Geofence creation skipped (duplicate or invalid)")
                        }
                        isInitialized = true
                    }.onFailure { e ->
                        Log.e(TAG, "Failed to pull barrier info: ${e.message}")
                        _initState.value = GeofenceInitState.Error(e.message ?: "拉取围栏配置失败")
                    }
                }
        }
    }

    /**
     * 移除所有本地围栏
     */
    fun removeGeofence() {
        geofenceManager.removeAllGeofences()
        isInitialized = false
        _initState.value = GeofenceInitState.Idle
    }
}

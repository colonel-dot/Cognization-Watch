package geofence.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.geofence.model.BarrierInfo
import geofence.manager.GeofenceManager
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

class GeofenceViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CognitiveGeoViewModel"
    }

    private val geofenceManager = GeofenceManager(application)

    private val _initState = MutableStateFlow<GeofenceInitState>(GeofenceInitState.Idle)
    val initState: StateFlow<GeofenceInitState> = _initState.asStateFlow()

    private var isInitialized = false

    init {
        geofenceManager.setFenceListener { geoFenceList, errorCode, errorMsg ->
            if (errorCode == 0 && !geoFenceList.isNullOrEmpty()) {
                Log.d(TAG, "Geofence created successfully: ${geoFenceList[0].fenceId}")
            } else {
                Log.e(TAG, "Geofence create failed: $errorCode - $errorMsg")
            }
        }

        tryRestoreFromLocal()
    }

    private fun tryRestoreFromLocal() {
        val restored = geofenceManager.restoreGeofenceIfExists()
        if (restored) {
            Log.d(TAG, "Geofence restored from local storage")
            _initState.value = GeofenceInitState.Success(geofenceManager.getSavedBarrierInfo()!!)
            isInitialized = true
        }
    }

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

    fun removeGeofence() {
        geofenceManager.removeAllGeofences()
        isInitialized = false
        _initState.value = GeofenceInitState.Idle
    }
}

package com.example.bridge.geofence.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.geofence.model.ElderMovement
import com.example.common.geofence.model.BarrierInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.bridge.geofence.network.GeoNetworkRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlin.onSuccess

enum class DataChangeType {
    BARRIER_INFO,    // 围栏信息变更
    ELDER_MOVEMENT   // 老人轨迹变更
}

sealed class FenceUiState {
    object Idle : FenceUiState() // 初始状态
    object Loading : FenceUiState() // 加载中
    data class GetSuccess(val data: BarrierInfo) : FenceUiState()  // 获取成功
    data class PostSuccess(val msg: String = "围栏信息提交成功") : FenceUiState() // 提交成功
    data class Error(val msg: String) : FenceUiState() // 错误状态
}

sealed class MovementUiState {
    object Idle : MovementUiState()
    object Loading : MovementUiState()
    data class GetSuccess(val data: ElderMovement) : MovementUiState()
    data class PostSuccess(val msg: String = "老人轨迹提交成功") : MovementUiState()
    data class Error(val msg: String) : MovementUiState()
}

class GeoViewModel(application: Application) : AndroidViewModel(application) {
    private val geoRepo = GeoNetworkRepository

    // 围栏信息
    private val _fenceUiState = MutableStateFlow<FenceUiState>(FenceUiState.Idle)
    val fenceUiState: StateFlow<FenceUiState> = _fenceUiState.asStateFlow()

    // Java 使用
    fun getBarrierUiState(): LiveData<FenceUiState> = _fenceUiState.asLiveData()
    fun isBarrierPostSuccess(state: FenceUiState): Boolean = state is FenceUiState.PostSuccess
    fun isBarrierError(state: FenceUiState): Boolean = state is FenceUiState.Error
    fun getBarrierErrorMsg(state: FenceUiState): String? = (state as? FenceUiState.Error)?.msg

    // 老人轨迹
    private val _movementUiState = MutableStateFlow<MovementUiState>(MovementUiState.Idle)
    val movementUiState: StateFlow<MovementUiState> = _movementUiState.asStateFlow()

    // 数据变更通知
    private val _dataUpdated = MutableSharedFlow<Unit>(replay = 0)
    val dataUpdated = _dataUpdated.asSharedFlow()

    private val _dataChanged = MutableSharedFlow<DataChangeType>(replay = 0)
    val dataChanged = _dataChanged.asSharedFlow()
    fun getBarrierInfo(childname: String) {
        _fenceUiState.value = FenceUiState.Loading
        viewModelScope.launch {
            geoRepo.getFenceInfo(childname)
                .collect { result ->
                    result.onSuccess { fenceInfo ->
                        _fenceUiState.value = FenceUiState.GetSuccess(fenceInfo)
                        notifyDataChanged(DataChangeType.BARRIER_INFO)
                    }.onFailure { e ->
                        _fenceUiState.value = FenceUiState.Error(e.message ?: "获取围栏信息失败")
                    }
                }
        }
    }

    fun postBarrierInfo(eldername: String, barrierInfo: BarrierInfo) {
        _fenceUiState.value = FenceUiState.Loading
        viewModelScope.launch {
            geoRepo.postBarrierInfo(eldername, barrierInfo)
                .collect { result ->
                    result.onSuccess {
                        _fenceUiState.value = FenceUiState.PostSuccess()
                        notifyDataChanged(DataChangeType.BARRIER_INFO)
                    }.onFailure { e ->
                        _fenceUiState.value = FenceUiState.Error(e.message ?: "提交围栏信息失败")
                    }
                }
        }
    }

    fun getElderMovement(eldername: String) {
        _movementUiState.value = MovementUiState.Loading
        viewModelScope.launch {
            geoRepo.getElderMovement(eldername)
                .collect { result ->
                    result.onSuccess { elderMovement ->
                        _movementUiState.value = MovementUiState.GetSuccess(elderMovement)
                        notifyDataChanged(DataChangeType.ELDER_MOVEMENT)
                    }.onFailure { e ->
                        _movementUiState.value = MovementUiState.Error(e.message ?: "获取老人轨迹失败")
                    }
                }
        }
    }

    fun postElderMovement(childname: String, elderMovement: ElderMovement) {
        _movementUiState.value = MovementUiState.Loading
        viewModelScope.launch {
            geoRepo.postElderMovement(childname, elderMovement)
                .collect { result ->
                    result.onSuccess {
                        _movementUiState.value = MovementUiState.PostSuccess()
                        notifyDataChanged(DataChangeType.ELDER_MOVEMENT)
                    }.onFailure { e ->
                        _movementUiState.value = MovementUiState.Error(e.message ?: "提交老人轨迹失败")
                    }
                }
        }
    }

    private fun notifyDataChanged(type: DataChangeType) {
        viewModelScope.launch {
            _dataChanged.emit(type)
            _dataUpdated.emit(Unit)
        }
    }

    fun resetBarrierState() {
        _fenceUiState.value = FenceUiState.Idle
    }

    fun resetMovementState() {
        _movementUiState.value = MovementUiState.Idle
    }
}
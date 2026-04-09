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

/**
 * 数据变更类型（用于区分不同数据的更新）
 */
enum class DataChangeType {
    BARRIER_INFO,    // 围栏信息变更
    ELDER_MOVEMENT   // 老人轨迹变更
}

/**
 * 围栏信息相关 UI 状态
 */
sealed class FenceUiState {
    object Idle : FenceUiState()          // 初始状态
    object Loading : FenceUiState()       // 加载中
    data class GetSuccess(val data: BarrierInfo) : FenceUiState()  // 获取成功
    data class PostSuccess(val msg: String = "围栏信息提交成功") : FenceUiState() // 提交成功
    data class Error(val msg: String) : FenceUiState() // 错误状态
}

/**
 * 老人轨迹相关 UI 状态
 */
sealed class MovementUiState {
    object Idle : MovementUiState()
    object Loading : MovementUiState()
    data class GetSuccess(val data: ElderMovement) : MovementUiState()
    data class PostSuccess(val msg: String = "老人轨迹提交成功") : MovementUiState()
    data class Error(val msg: String) : MovementUiState()
}

class GeoViewModel(application: Application) : AndroidViewModel(application) {
    // 仓库单例
    private val geoRepo = GeoNetworkRepository

    // ==================== 围栏信息状态（内部可变，外部只读） ====================
    private val _fenceUiState = MutableStateFlow<FenceUiState>(FenceUiState.Idle)
    val fenceUiState: StateFlow<FenceUiState> = _fenceUiState.asStateFlow()

    // Java interop: expose as LiveData with class tags for safe instanceof checks
    fun getBarrierUiState(): LiveData<FenceUiState> = _fenceUiState.asLiveData()

    // Java interop: helper to check state type (avoids sealed class instanceof issues in Java)
    fun isBarrierPostSuccess(state: FenceUiState): Boolean = state is FenceUiState.PostSuccess
    fun isBarrierError(state: FenceUiState): Boolean = state is FenceUiState.Error
    fun getBarrierErrorMsg(state: FenceUiState): String? = (state as? FenceUiState.Error)?.msg

    // ==================== 老人轨迹状态 ====================
    private val _movementUiState = MutableStateFlow<MovementUiState>(MovementUiState.Idle)
    val movementUiState: StateFlow<MovementUiState> = _movementUiState.asStateFlow()

    // ==================== 新增：数据变更通知 Flow ====================
    // 1. 通用通知（仅告知“有数据更新”，无具体类型）
    private val _dataUpdated = MutableSharedFlow<Unit>(replay = 0)
    val dataUpdated = _dataUpdated.asSharedFlow()

    // 2. 带类型的通知（告知“某类数据更新”，便于 UI 区分处理）
    private val _dataChanged = MutableSharedFlow<DataChangeType>(replay = 0)
    val dataChanged = _dataChanged.asSharedFlow()

    // ==================== 原有业务方法（增加数据变更通知） ====================
    fun getBarrierInfo(childname: String) {
        _fenceUiState.value = FenceUiState.Loading

        viewModelScope.launch {
            geoRepo.getFenceInfo(childname)
                .collect { result ->
                    result.onSuccess { fenceInfo ->
                        _fenceUiState.value = FenceUiState.GetSuccess(fenceInfo)
                        // 发送数据变更通知
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
                        // 发送数据变更通知（提交成功后，后端数据已更新）
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
                        // 发送数据变更通知
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
                        // 发送数据变更通知（提交成功后，后端数据已更新）
                        notifyDataChanged(DataChangeType.ELDER_MOVEMENT)
                    }.onFailure { e ->
                        _movementUiState.value = MovementUiState.Error(e.message ?: "提交老人轨迹失败")
                    }
                }
        }
    }

    // ==================== 新增：数据变更通知方法 ====================
    /**
     * 发送数据变更通知（内部封装，统一处理）
     * @param type 变更的数据类型
     */
    private fun notifyDataChanged(type: DataChangeType) {
        viewModelScope.launch {
            // 发送带类型的通知
            _dataChanged.emit(type)
            // 发送通用通知（可选，根据 UI 需求选择）
            _dataUpdated.emit(Unit)
        }
    }

    // 可选：重置状态（如页面刷新、返回时）
    fun resetBarrierState() {
        _fenceUiState.value = FenceUiState.Idle
    }

    fun resetMovementState() {
        _movementUiState.value = MovementUiState.Idle
    }
}
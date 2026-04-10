package com.example.common.bind_device

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import com.example.common.persistense.AppDatabase
import com.example.common.persistense.behavior.DailyBehaviorEntity
import com.example.common.persistense.risk.DailyRiskEntity

private const val TAG = "BindViewModel"

sealed class BindAndLoadState {
    object BindSuccess : BindAndLoadState() // 绑定成功
    object DataLoading : BindAndLoadState() // 数据加载中
    data class DataLoadSuccess(
        val behaviorList: List<DailyBehaviorEntity>,
        val riskList: List<DailyRiskEntity>
    ) : BindAndLoadState() // 数据加载成功
    data class DataLoadFailure(val message: String) : BindAndLoadState() // 数据加载失败
    object BindFailure : BindAndLoadState() // 绑定失败
}

class BindViewModel(application: Application) : AndroidViewModel(application) {
    private val _bindAndLoadState = MutableSharedFlow<BindAndLoadState>()
    val bindAndLoadState: SharedFlow<BindAndLoadState> = _bindAndLoadState

    private val database by lazy { AppDatabase.getDatabase(getApplication()) }
    private val behaviorDao by lazy { database.dailyBehaviorDao() }
    private val riskDao by lazy { database.dailyRiskDao() }

    fun bind(bindname: String) {
        viewModelScope.launch {
            // TODO: bindname
            val mname = getApplication<Application>()
                .getSharedPreferences("login_status", Context.MODE_PRIVATE)
                .getString("username", "") ?: ""

            try {
                BindRepository.bind(BindRequest(mname, bindname)).collect { bindResult ->
                    bindResult.fold(
                        onSuccess = { bindResponse ->
                            if (bindResponse.code == 200) {
                                BindStatusManager.saveBindStatus(
                                    getApplication(),
                                    true,
                                    bindname
                                )
                                Log.d(TAG, "bind: 绑定成功，开始加载数据")
                                _bindAndLoadState.emit(BindAndLoadState.BindSuccess)

                                loadOtherData(bindname)
                            } else {
                                _bindAndLoadState.emit(BindAndLoadState.BindFailure)
                            }
                        },
                        onFailure = { e ->
                            Log.d(TAG, "bind:${e.message}")
                            if (e.message.equals("HTTP 403 Forbidden")) {
                                BindStatusManager.saveBindStatus(
                                    getApplication(),
                                    true,
                                    bindname
                                )
                                Log.d(TAG, "bind: 重复绑定成功，开始加载数据")
                                _bindAndLoadState.emit(BindAndLoadState.BindSuccess)

                                loadOtherData(bindname)
                            } else {
                                _bindAndLoadState.emit(BindAndLoadState.BindFailure)
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                Log.d(TAG, "bind: $e")
                _bindAndLoadState.emit(BindAndLoadState.BindFailure)
            }
        }
    }

    private suspend fun loadOtherData(account: String) {
        _bindAndLoadState.emit(BindAndLoadState.DataLoading)

        try {
            val behaviorDeferred = viewModelScope.async {
                BindRepository.getOtherAllBehavior(account).first()
            }
            val riskDeferred = viewModelScope.async {
                BindRepository.getOtherAllRisk(account).first()
            }

            val behaviorResult = behaviorDeferred.await()
            val riskResult = riskDeferred.await()

            val behaviorList = behaviorResult.getOrThrow()
            val riskList = riskResult.getOrThrow()

            behaviorDao.insertAll(behaviorList)
            riskDao.insertAll(riskList)

            _bindAndLoadState.emit(BindAndLoadState.DataLoadSuccess(behaviorList, riskList))

        } catch (e: Exception) {
            Log.d(TAG, "loadOtherData: ${e.message}")
            _bindAndLoadState.emit(BindAndLoadState.DataLoadFailure(e.message ?: "数据加载失败"))
        }
    }
}
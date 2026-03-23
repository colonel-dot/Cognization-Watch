package bind_device

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
import persistense.AppDatabase
import persistense.behavior.DailyBehaviorEntity
import persistense.risk.DailyRiskEntity

private const val TAG = "BindViewModel"

// 新增：绑定+数据加载的完整状态
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
    // 替换原有的Boolean类型Flow，用密封类传递完整状态
    private val _bindAndLoadState = MutableSharedFlow<BindAndLoadState>()
    val bindAndLoadState: SharedFlow<BindAndLoadState> = _bindAndLoadState

    // 抽取数据库实例（复用）
    private val database by lazy { AppDatabase.getDatabase(getApplication()) }
    private val behaviorDao by lazy { database.dailyBehaviorDao() }
    private val riskDao by lazy { database.dailyRiskDao() }

    fun bind(bindname: String) {
        viewModelScope.launch {
            val mname = getApplication<Application>()
                .getSharedPreferences("login_status", Context.MODE_PRIVATE)
                .getString("username", "") ?: ""

            try {
                BindRepository.bind(BindRequest(mname, bindname)).collect { bindResult ->
                    bindResult.fold(
                        onSuccess = { bindResponse ->
                            if (bindResponse.code == 200) {
                                // 1. 绑定成功：保存状态 + 通知UI
                                BindStatusManager.saveBindStatus(
                                    getApplication(),
                                    true,
                                    bindname
                                )
                                Log.d(TAG, "bind: 绑定成功，开始加载数据")
                                _bindAndLoadState.emit(BindAndLoadState.BindSuccess)

                                // 2. 绑定成功后，调用两个数据接口加载数据
                                loadOtherData(bindname)
                            } else {
                                // 绑定失败（接口返回非200）
                                _bindAndLoadState.emit(BindAndLoadState.BindFailure)
                            }
                        },
                        onFailure = { e ->
                            // 绑定请求失败（网络异常等）
                            Log.d(TAG, "bind:${e.message}")
                            _bindAndLoadState.emit(BindAndLoadState.BindFailure)
                        }
                    )
                }
            } catch (e: Exception) {
                Log.d(TAG, "bind: $e")
                _bindAndLoadState.emit(BindAndLoadState.BindFailure)
            }
        }
    }

    // 新增：封装数据加载逻辑（绑定成功后调用）
    private suspend fun loadOtherData(account: String) {
        // 1. 通知UI：数据开始加载（可显示加载框）
        _bindAndLoadState.emit(BindAndLoadState.DataLoading)

        try {
            // 2. 并行请求两个接口（提升效率，async+await）
            val behaviorDeferred = viewModelScope.async {
                BindRepository.getOtherAllBehavior(account).first() // first()：获取单次结果，无需持续collect
            }
            val riskDeferred = viewModelScope.async {
                BindRepository.getOtherAllRisk(account).first()
            }

            // 3. 等待两个请求完成
            val behaviorResult = behaviorDeferred.await()
            val riskResult = riskDeferred.await()

            // 4. 处理结果（两个都成功才视为数据加载成功）
            val behaviorList = behaviorResult.getOrThrow()
            val riskList = riskResult.getOrThrow()

            // 5. 缓存到数据库（可选，根据需求）
            behaviorDao.insertAll(behaviorList)
            riskDao.insertAll(riskList)

            // 6. 通知UI：数据加载成功
            _bindAndLoadState.emit(BindAndLoadState.DataLoadSuccess(behaviorList, riskList))

        } catch (e: Exception) {
            // 任意一个请求失败，通知UI失败
            Log.d(TAG, "loadOtherData: ${e.message}")
            _bindAndLoadState.emit(BindAndLoadState.DataLoadFailure(e.message ?: "数据加载失败"))
        }
    }
}
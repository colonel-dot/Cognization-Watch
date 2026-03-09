package other

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import persistense.AppDatabase
import repository.NetWorkRepository
import risk.model.toEntity
import user.UserManager
import java.time.LocalDate

private const val TAG = "OtherReportViewModel"

// 定义操作结果密封类，让UI层区分不同操作的结果
sealed class OtherReportResult {
    // 行为数据加载成功
    object BehaviorLoadSuccess : OtherReportResult()
    // 风险数据加载成功
    object RiskLoadSuccess : OtherReportResult()
    // 加载失败（带错误信息）
    data class LoadFailure(val message: String) : OtherReportResult()
}

class OtherReportViewModel(application: Application) : AndroidViewModel(application) {
    // 抽取数据库实例，避免重复获取
    private val database by lazy { AppDatabase.getDatabase(getApplication()) }
    private val dailyBehaviorDao by lazy { database.dailyBehaviorDao() }
    private val dailyRiskDao by lazy { database.dailyRiskDao() }

    // 私有可修改的Flow，用于发送操作结果给UI层
    private val _operationResult = MutableSharedFlow<OtherReportResult>()
    // 公开只读的Flow，供UI层收集
    val operationResult: SharedFlow<OtherReportResult> = _operationResult

    fun getOtherDailyBehavior(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val otherId = UserManager.getOtherId()
            // 空值校验：避免传入null导致接口请求失败
            if (otherId.isNullOrBlank()) {
                val errorMsg = "用户ID为空，无法获取行为数据"
                Log.d(TAG, "getOtherDailyBehavior: $errorMsg")
                _operationResult.emit(OtherReportResult.LoadFailure(errorMsg))
                return@launch
            }

            NetWorkRepository.getOtherDailyBehavior(otherId, date).collect { result ->
                result.onSuccess { dailyBehavior ->
                    dailyBehaviorDao.insert(dailyBehavior)
                    // 通知UI层：加载成功
                    _operationResult.emit(OtherReportResult.BehaviorLoadSuccess)
                }.onFailure { exception ->
                    val errorMsg = "获取行为数据失败：${exception.message}"
                    Log.d(TAG, errorMsg)
                    // 通知UI层：加载失败
                    _operationResult.emit(OtherReportResult.LoadFailure(errorMsg))
                }
            }
        }
    }

    fun getOtherDailyRisk(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val otherId = UserManager.getOtherId()
            if (otherId.isNullOrBlank()) {
                val errorMsg = "用户ID为空，无法获取风险数据"
                Log.d(TAG, "getOtherDailyRisk: $errorMsg")
                _operationResult.emit(OtherReportResult.LoadFailure(errorMsg))
                return@launch
            }

            NetWorkRepository.getOtherDailyRisk(otherId, date).collect { result ->
                result.onSuccess { dailyRisk ->
                    dailyRiskDao.insert(dailyRisk.toEntity())
                    // 通知UI层：加载成功
                    _operationResult.emit(OtherReportResult.RiskLoadSuccess)
                }.onFailure { exception ->
                    val errorMsg = "获取风险数据失败：${exception.message}"
                    Log.d(TAG, errorMsg)
                    // 通知UI层：加载失败
                    _operationResult.emit(OtherReportResult.LoadFailure(errorMsg))
                }
            }
        }
    }
}
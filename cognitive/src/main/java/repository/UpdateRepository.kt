package repository

import android.util.Log
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import persistense.DailyBehaviorDao
import persistense.DailyBehaviorEntity
import risk.persistence.DailyRiskDao
import user.UserManager
import java.time.LocalDate

private const val TAG = "UpdateRepository"

class UpdateRepository(
    private val behaviorDao: DailyBehaviorDao,
    //private val riskDao: DailyRiskDao
) {
    // 1. 互斥锁保证线程安全（多协程修改 todayBehavior 时串行执行）
    private val mutex = Mutex()
    // 2. 内存缓存（私有，仅内部修改）
    private var todayBehavior: DailyBehaviorEntity? = null


    suspend fun initToday(today: LocalDate) {
        mutex.withLock {
            todayBehavior = behaviorDao.getOrInitTodayBehavior(today)
        }
    }

    suspend fun getTodayBehavior(): DailyBehaviorEntity {
        return mutex.withLock {
            todayBehavior ?: throw IllegalStateException("未调用 initToday 初始化今日数据")
        }
    }


    private suspend fun updateBehavior(
        updateAction: (DailyBehaviorEntity) -> DailyBehaviorEntity
    ) {
        mutex.withLock {
            // 步骤1：判空（未初始化直接返回）
            val oldEntity = todayBehavior ?: return

            // 步骤2：执行自定义更新逻辑（修改指定字段）
            val newEntity = updateAction(oldEntity)

            // 步骤3：更新内存缓存
            todayBehavior = newEntity

            // 步骤4：更新本地数据库
            behaviorDao.update(newEntity)

            // 步骤5：同步网络（带异常处理）
            syncToNetwork(newEntity)
        }
    }

    // 4. 具体业务方法（极简，仅关注字段修改）
    suspend fun updateSchulte(score: Double) {
        updateBehavior { it.copy(schulte16TimeSec = score) }
    }

    suspend fun updateSpeechScore(score: Double) {
        updateBehavior { it.copy(speechScore = score) }
    }

    suspend fun updateSteps(steps: Int) {
        updateBehavior { it.copy(steps = steps) }
    }

    suspend fun updateWakeTime(wakeMinute: Int) {
        updateBehavior { it.copy(wakeMinute = wakeMinute) }
    }

    suspend fun updateSleepTime(sleepMinute: Int) {
        updateBehavior { it.copy(sleepMinute = sleepMinute) }
    }

    // 5. 通用网络同步逻辑（集中处理异常和日志）
    private suspend fun syncToNetwork(entity: DailyBehaviorEntity) {
        NetWorkRepository.updateDailyBehavior(
            account = UserManager.getUserId(),
            date = entity.date,
            record = entity
        )
            // 捕获网络调用异常，避免崩溃
            .catch { e ->
                Log.e(TAG, "同步每日行为数据到网络失败", e)
            }
            .collect { result ->
                Log.d(TAG, "同步网络结果：$result")
            }
    }
}
package repository

import android.util.Log
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.common.persistense.AppDatabase
import com.example.common.persistense.behavior.DailyBehaviorEntity
import user.UserManager

private const val TAG = "UpdateRepository"

object UpdateRepository{
    // 1. 互斥锁保证线程安全（多协程修改 todayBehavior 时串行执行）
    private val mutex = Mutex()
    // 2. 内存缓存（私有，仅内部修改）
    private var mtodayBehavior: DailyBehaviorEntity? = null
    private val behaviorDao by lazy { AppDatabase.getDatabase(AppDatabase.getAppContext()).dailyBehaviorDao() }

    fun initToday(todayBehavior: DailyBehaviorEntity? = null) {
            mtodayBehavior = todayBehavior
    }

    suspend fun getTodayBehavior(): DailyBehaviorEntity {
        return mutex.withLock {
            mtodayBehavior ?: throw IllegalStateException("未调用 initToday 初始化今日数据")
        }
    }


    private suspend fun updateBehavior(
        updateAction: (DailyBehaviorEntity) -> DailyBehaviorEntity
    ) {
        mutex.withLock {
            Log.d(TAG, "updateBehavior: 这里的 todayBehavior = $mtodayBehavior")

            val oldEntity = mtodayBehavior ?: return

            val newEntity = updateAction(oldEntity)

            mtodayBehavior = newEntity
            Log.d(TAG, "updateBehavior: 现在的mEntity是$mtodayBehavior newEntity是$newEntity")
            behaviorDao.update(newEntity)
            Log.d(TAG, "updateBehavior: 更新了本地数据库")

            syncToNetwork(newEntity)
        }
    }

    // 4. 具体业务方法（极简，仅关注字段修改）
    suspend fun update16Schulte(time: Double) {
        updateBehavior { it.copy(schulte16TimeSec = time) }
    }

    suspend fun update25Schulte(time: Double) {
        updateBehavior { it.copy(schulte25TimeSec = time) }
    }

    suspend fun updateSpeechScore(score: Double) {
        Log.d(TAG, "updateSpeechScore: 这里的speechScore是$score")
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
    
    suspend fun updateScheduleTime(wakeMinute: Int, sleepMinute: Int) {
        Log.d(TAG, "updateScheduleTime: 要在更新仓库中更新时间了。wakeMinute = $wakeMinute sleepMinute = $sleepMinute")
        updateBehavior { it.copy( wakeMinute = wakeMinute, sleepMinute = sleepMinute) }
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
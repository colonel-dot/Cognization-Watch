package sports.data

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.*
import persistense.DailyBehaviorDao
import java.time.LocalDate

private const val TAG = "StepRepository"

/**
 * 职责：
 * 1. 只负责「今日步数」统计
 * 2. 内存实时累积，定时批量落库
 */
class StepRepository(
    private val sensorManager: SensorManager,
    private val dao: DailyBehaviorDao
) : SensorEventListener {

    /** Service 生命周期作用域 */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 计步传感器（唯一需要的） */
    private val stepCounter =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    /** -------- 内存态 -------- */
    private var todayDate: LocalDate = LocalDate.now()
    private var todayBaseCounter = 0f
    private var todaySteps = 0

    /** 是否有变更需要落库 */
    @Volatile
    private var dirty = false

    // -------------------- 对外 --------------------

    fun start() {
        scope.launch {
            initToday()
            startFlushTicker()
        }

        stepCounter?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)

        // Service 结束前强制落库
        runBlocking {
            flushToDb()
        }

        scope.cancel()
    }

    // -------------------- 初始化 / 跨天 --------------------

    private suspend fun initToday() {
        todayDate = LocalDate.now()
        val entity = dao.getOrInitTodayBehavior(todayDate)

        // 从数据库恢复（防止 Service 重启）
        todaySteps = entity.steps ?: 0

        todayBaseCounter = 0f
        dirty = false
    }

    private suspend fun onNewDay(newDate: LocalDate) {
        flushToDb()

        todayDate = newDate
        dao.getOrInitTodayBehavior(newDate)

        todayBaseCounter = 0f
        todaySteps = 0
        dirty = false
    }

    // -------------------- 批量落库 --------------------

    private fun startFlushTicker() {
        scope.launch {
            while (isActive) {
                delay(30_000)
                flushToDb()
            }
        }
    }

    private suspend fun flushToDb() {
        if (!dirty) return

        Log.d(TAG, "flushToDb() steps=$todaySteps")

        dao.updateSteps(
            date = todayDate,
            steps = todaySteps
        )

        dirty = false
    }

    // -------------------- 传感器回调 --------------------

    override fun onSensorChanged(event: SensorEvent) {
        val today = LocalDate.now()
        if (today != todayDate) {
            scope.launch { onNewDay(today) }
            return
        }

        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val total = event.values[0]

            if (todayBaseCounter == 0f) {
                todayBaseCounter = total
            }

            val newSteps =
                (total - todayBaseCounter).toInt().coerceAtLeast(0)

            if (newSteps != todaySteps) {
                todaySteps = newSteps
                dirty = true
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

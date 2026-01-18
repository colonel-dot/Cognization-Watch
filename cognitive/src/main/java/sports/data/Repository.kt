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
 * 1. 实时接收传感器数据（只更新内存）
 * 2. 定时 & 关键节点批量写入数据库
 */
class StepRepository(
    private val sensorManager: SensorManager,
    private val dao: DailyBehaviorDao
) : SensorEventListener {

    /** 协程作用域（Service 生命周期） */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 传感器 */
    private val stepCounter =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetector =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    /** -------- 内存态数据（实时） -------- */
    private var todayDate: LocalDate = LocalDate.now()

    private var todayBaseCounter = 0f
    private var todaySteps = 0
    private var todayActiveTime = 0
    private var todayRestTime = 0

    private var lastStepTime = 0L

    /** 是否有数据变更（脏标记） */
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
        stepDetector?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)

        // ★ 关键：停服务前强制落库一次
        runBlocking {
            flushToDb()
        }

        scope.cancel()
    }

    // -------------------- 初始化 --------------------

    private suspend fun initToday() {
        todayDate = LocalDate.now()
        val entity = dao.getOrInitTodayBehavior(todayDate)

        // ★ 从数据库恢复数据，防止 Service 重启丢失
        todaySteps = entity.steps ?: 0
        todayActiveTime = entity.activeTime ?: 0
        todayRestTime = entity.restTime ?: 0

        todayBaseCounter = 0f
        lastStepTime = 0L
        dirty = false
    }

    private suspend fun onNewDay(newDate: LocalDate) {
        // 跨天前先落库
        flushToDb()

        todayDate = newDate
        dao.getOrInitTodayBehavior(newDate)

        todayBaseCounter = 0f
        todaySteps = 0
        todayActiveTime = 0
        todayRestTime = 0
        lastStepTime = 0L
        dirty = false
    }

    // -------------------- 定时批量落库 --------------------

    /**
     * 每 30 秒检查一次，只在数据变更时写数据库
     */
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

        Log.d(TAG, "flushToDb() steps=$todaySteps active=$todayActiveTime rest=$todayRestTime")

        dao.updateSports(
            date = todayDate,
            steps = todaySteps,
            activeTime = todayActiveTime,
            restTime = todayRestTime
        )

        dirty = false
    }

    // -------------------- 时间统计（每秒） --------------------

    private fun tickTime() {
        if (lastStepTime == 0L) return

        val now = System.currentTimeMillis()
        if (now - lastStepTime <= 30_000) {
            todayActiveTime++
        } else {
            todayRestTime++
        }

        dirty = true
    }

    // -------------------- 传感器回调 --------------------

    override fun onSensorChanged(event: SensorEvent) {
        val today = LocalDate.now()
        if (today != todayDate) {
            scope.launch { onNewDay(today) }
            return
        }

        when (event.sensor.type) {

            Sensor.TYPE_STEP_COUNTER -> {
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

            Sensor.TYPE_STEP_DETECTOR -> {
                lastStepTime = System.currentTimeMillis()
                tickTime()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

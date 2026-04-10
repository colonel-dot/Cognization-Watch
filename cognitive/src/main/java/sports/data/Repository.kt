package sports.data

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.*
import com.example.common.persistense.behavior.DailyBehaviorDao
import repository.UpdateRepository
import java.time.LocalDate

private const val TAG = "StepRepository"

class StepRepository(
    private val sensorManager: SensorManager,
    private val dao: DailyBehaviorDao
) : SensorEventListener {

    // Service 生命周期作用域
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 计步传感器
    private val stepCounter =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var todayDate: LocalDate = LocalDate.now()
    private var todayBaseCounter = 0f
    private var todaySteps = 0

    @Volatile
    private var dirty = false


    fun start() {
        scope.launch {
            initToday()
            startFlushTicker()
        }

        stepCounter?.let {
            try {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register sensor listener", e)
            }
        }
    }

    fun stop() {
        try {
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister sensor listener", e)
        }

        // Service 结束前强制落库
        runBlocking {
            flushToDb()
        }

        scope.cancel()
    }


    private suspend fun initToday() {
        try {
            todayDate = LocalDate.now()
            val entity = dao.getOrInitTodayBehavior(todayDate)

            // 从数据库恢复（防止 Service 重启）
            todaySteps = entity.steps ?: 0

            todayBaseCounter = 0f
            dirty = false
        } catch (e: Exception) {
            Log.e(TAG, "initToday failed", e)
        }
    }

    private suspend fun onNewDay(newDate: LocalDate) {
        try {
            flushToDb()

            todayDate = newDate
            dao.getOrInitTodayBehavior(newDate)

            todayBaseCounter = 0f
            todaySteps = 0
            dirty = false
        } catch (e: Exception) {
            Log.e(TAG, "onNewDay failed", e)
        }
    }


    private fun startFlushTicker() {
        scope.launch {
            try {
                while (isActive) {
                    delay(30_000)
                    flushToDb()
                }
            } catch (e: Exception) {
                Log.e(TAG, "startFlushTicker failed", e)
            }
        }
    }

    private suspend fun flushToDb() {
        if (!dirty) return

        try {
            Log.d(TAG, "flushToDb() steps=$todaySteps")
            UpdateRepository.updateSteps(steps = todaySteps)
            dirty = false
        } catch (e: Exception) {
            Log.e(TAG, "flushToDb failed", e)
        }
    }


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

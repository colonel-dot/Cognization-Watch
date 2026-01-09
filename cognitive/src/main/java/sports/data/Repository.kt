package sports.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class StepRepository(context: Context) : SensorEventListener {

    private val sm =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepCounter = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetector = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val sp = context.getSharedPreferences("step", Context.MODE_PRIVATE)

    // 对外回调
    var onStepChanged: ((Int) -> Unit)? = null
    var onTimeChanged: ((Int, Int) -> Unit)? = null

    // 当天基准数据
    private var initialSteps = 0f
    private var lastDate = ""

    // 今日数据
    private var todaySteps = 0
    private var todayActiveTime = 0
    private var todayRestTime = 0

    // 最近一次走路时间
    private var lastStepTime = 0L

    //  定时器（核心）
    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            tickTime()
            handler.postDelayed(this, 1000)
        }
    }

    // ------------------ 对外接口 ------------------

    fun start() {
        loadState()

        stepCounter?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        stepDetector?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        handler.post(timerRunnable) // 启动实时计时
    }

    fun stop() {
        sm.unregisterListener(this)
        handler.removeCallbacks(timerRunnable)
    }

    // ------------------ 核心计时逻辑（真正修复点） ------------------

    private fun tickTime() {
        if (lastStepTime == 0L) return

        val now = System.currentTimeMillis()
        val gapSec = ((now - lastStepTime) / 1000).toInt().coerceAtLeast(1)

        if (gapSec <= 30) {
            // 最近30秒走过 → 运动中
            todayActiveTime += 1
        } else {
            // 超过30秒没走 → 休息中
            todayRestTime += 1
        }

        onTimeChanged?.invoke(todayActiveTime, todayRestTime)
    }

    // ------------------ 传感器回调 ------------------

    override fun onSensorChanged(event: SensorEvent) {
        val today = getToday()

        //  跨天处理
        if (today != lastDate && lastDate.isNotEmpty()) {
            saveYesterdayRecord()

            initialSteps = event.values[0]
            todaySteps = 0
            todayActiveTime = 0
            todayRestTime = 0
            lastDate = today
            saveState()
        }

        when (event.sensor.type) {

            Sensor.TYPE_STEP_COUNTER -> {
                val total = event.values[0]

                if (initialSteps == 0f) {
                    initialSteps = total
                    lastDate = today
                    saveState()
                }

                todaySteps = (total - initialSteps).toInt()
                onStepChanged?.invoke(todaySteps)
            }

            // 只更新时间戳，不再在这里算休息
            Sensor.TYPE_STEP_DETECTOR -> {
                lastStepTime = System.currentTimeMillis()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ------------------ 历史持久化 ------------------

    private fun saveYesterdayRecord() {
        val historyJson = sp.getString("history", "[]")!!
        val list = JSONArray(historyJson)

        val obj = JSONObject().apply {
            put("date", lastDate)
            put("steps", todaySteps)
            put("activeTime", todayActiveTime)
            put("restTime", todayRestTime)
        }

        list.put(obj)
        sp.edit().putString("history", list.toString()).apply()
    }

    private fun saveState() {
        sp.edit()
            .putFloat("initialSteps", initialSteps)
            .putString("lastDate", lastDate)
            .apply()
    }

    private fun loadState() {
        initialSteps = sp.getFloat("initialSteps", 0f)
        lastDate = sp.getString("lastDate", "") ?: ""
    }

    private fun getToday(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}

package sports.data

import android.content.Context
import android.hardware.*
import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class StepRepository(private val context: Context) : SensorEventListener {

    private val sm =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepCounter = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetector = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val sp = context.getSharedPreferences("step", Context.MODE_PRIVATE)

    var onStepChanged: ((Int) -> Unit)? = null
    var onTimeChanged: ((Int, Int) -> Unit)? = null

    private var initialSteps = 0f
    private var lastDate = ""

    private var todaySteps = 0
    private var todayActiveTime = 0
    private var todayRestTime = 0

    private var lastStepTime = 0L

    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            tickTime()
            handler.postDelayed(this, 1000)
        }
    }

    private val dateFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun start() {
        loadState()
        stepCounter?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        stepDetector?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        handler.post(timerRunnable)
    }

    fun stop() {
        sm.unregisterListener(this)
        handler.removeCallbacks(timerRunnable)
        saveState()
    }

    private fun tickTime() {
        val now = System.currentTimeMillis()
        val gapSec =
            if (lastStepTime == 0L) Int.MAX_VALUE
            else TimeUnit.MILLISECONDS.toSeconds(now - lastStepTime).toInt()

        if (gapSec < 30) {
            todayActiveTime++
        } else {
            todayRestTime++
        }

        onTimeChanged?.invoke(todayActiveTime, todayRestTime)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val today = getToday()

        if (today != lastDate && lastDate.isNotEmpty()) {
            saveYesterday()
            resetToday(event.values[0], today)
        }

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val total = event.values[0]
                if (initialSteps == 0f) {
                    initialSteps = total
                    lastDate = today
                }
                todaySteps = (total - initialSteps).toInt()
                onStepChanged?.invoke(todaySteps)
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                lastStepTime = System.currentTimeMillis()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun saveYesterday() {
        val history = JSONArray(sp.getString("history", "[]")!!)
        val obj = JSONObject().apply {
            put("date", lastDate)
            put("steps", todaySteps)
            put("activeTime", todayActiveTime)
            put("restTime", todayRestTime)
        }
        history.put(obj)
        sp.edit().putString("history", history.toString()).apply()
    }

    private fun resetToday(counterBase: Float, today: String) {
        initialSteps = counterBase
        todaySteps = 0
        todayActiveTime = 0
        todayRestTime = 0
        lastStepTime = 0L
        lastDate = today
        saveState()
    }

    private fun saveState() {
        sp.edit()
            .putFloat("initialSteps", initialSteps)
            .putString("lastDate", lastDate)
            .putInt("todayActiveTime", todayActiveTime)
            .putInt("todayRestTime", todayRestTime)
            .putLong("lastStepTime", lastStepTime)
            .apply()
    }

    private fun loadState() {
        initialSteps = sp.getFloat("initialSteps", 0f)
        lastDate = sp.getString("lastDate", "") ?: ""
        todayActiveTime = sp.getInt("todayActiveTime", 0)
        todayRestTime = sp.getInt("todayRestTime", 0)
        lastStepTime = sp.getLong("lastStepTime", 0L)
    }

    private fun getToday(): String = dateFormat.format(Date())
}

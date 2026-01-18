package schedule.vm

import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import persistense.DailyBehaviorDatabase
import java.util.*

data class ScreenEvent(val type: String, val time: Long)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val dailyBehaviorDatabase = DailyBehaviorDatabase.getDatabase(application)
    private val dailyBehaviorDao = dailyBehaviorDatabase.dailyBehaviorDao()

    val hours = (0..23).map { String.format("%02d", it) }
    val minutes = (0..59).map { String.format("%02d", it) }

    private val _bedTimeText = MutableLiveData<String>()
    val bedTimeText: LiveData<String> = _bedTimeText

    private val _wakeTimeText = MutableLiveData<String>()
    val wakeTimeText: LiveData<String> = _wakeTimeText

    var bedHourPos = 0
    var bedMinutePos = 0
    var wakeHourPos = 0
    var wakeMinutePos = 0

    // 防止旋转屏幕后重复初始化
    var hasInitBySystemEvents = false

    init {
        val (defaultSleep, defaultWake) = getDefaultSleepWakeTime()
        val sleepCal = Calendar.getInstance().apply { timeInMillis = defaultSleep }
        val wakeCal = Calendar.getInstance().apply { timeInMillis = defaultWake }

        onBedTimeSelected(
            String.format("%02d", sleepCal.get(Calendar.HOUR_OF_DAY)),
            String.format("%02d", sleepCal.get(Calendar.MINUTE)),
            sleepCal.get(Calendar.HOUR_OF_DAY),
            sleepCal.get(Calendar.MINUTE)
        )

        onWakeTimeSelected(
            String.format("%02d", wakeCal.get(Calendar.HOUR_OF_DAY)),
            String.format("%02d", wakeCal.get(Calendar.MINUTE)),
            wakeCal.get(Calendar.HOUR_OF_DAY),
            wakeCal.get(Calendar.MINUTE)
        )

        // 第一次初始化标记
        hasInitBySystemEvents = true
    }

    fun onBedTimeSelected(hour: String, minute: String, hourPos: Int, minutePos: Int) {
        _bedTimeText.value = "睡觉时间: $hour:$minute"
        bedHourPos = hourPos
        bedMinutePos = minutePos
    }

    fun onWakeTimeSelected(hour: String, minute: String, hourPos: Int, minutePos: Int) {
        _wakeTimeText.value = "起床时间: $hour:$minute"
        wakeHourPos = hourPos
        wakeMinutePos = minutePos
    }

    private fun getScreenEventsToday(): List<ScreenEvent> {
        val usm = context.getSystemService(Application.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 4)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = System.currentTimeMillis()

        val events = mutableListOf<ScreenEvent>()
        val usageEvents = usm.queryEvents(start, end)
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.SCREEN_INTERACTIVE ->
                    events.add(ScreenEvent("SCREEN_ON", event.timeStamp))

                UsageEvents.Event.SCREEN_NON_INTERACTIVE ->
                    events.add(ScreenEvent("SCREEN_OFF", event.timeStamp))
            }
        }
        return events.sortedBy { it.time }
    }

    private fun getDefaultSleepWakeTime(): Pair<Long, Long> {
        val events = getScreenEventsToday()
        val now = System.currentTimeMillis()

        if (events.isEmpty()) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 0)
            val sleep = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 7)
            cal.set(Calendar.MINUTE, 0)
            val wake = cal.timeInMillis
            return sleep to wake
        }

        val sleepTime = events.lastOrNull { it.type == "SCREEN_OFF" }?.time ?: now
        val wakeTime = events.firstOrNull { it.type == "SCREEN_ON" }?.time ?: now

        return sleepTime to wakeTime
    }

    /**  只第一次执行，旋转屏不再重跑 */
    fun refreshBySystemEvents() {
        if (hasInitBySystemEvents) return

        val (sleep, wake) = getDefaultSleepWakeTime()
        val sleepCal = Calendar.getInstance().apply { timeInMillis = sleep }
        val wakeCal = Calendar.getInstance().apply { timeInMillis = wake }

        onBedTimeSelected(
            String.format("%02d", sleepCal.get(Calendar.HOUR_OF_DAY)),
            String.format("%02d", sleepCal.get(Calendar.MINUTE)),
            sleepCal.get(Calendar.HOUR_OF_DAY),
            sleepCal.get(Calendar.MINUTE)
        )

        onWakeTimeSelected(
            String.format("%02d", wakeCal.get(Calendar.HOUR_OF_DAY)),
            String.format("%02d", wakeCal.get(Calendar.MINUTE)),
            wakeCal.get(Calendar.HOUR_OF_DAY),
            wakeCal.get(Calendar.MINUTE)
        )

        hasInitBySystemEvents = true
    }
}

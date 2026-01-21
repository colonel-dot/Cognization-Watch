package schedule.vm

import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import persistense.AppDatabase
import java.time.LocalDate
import java.util.*

data class ScreenEvent(val type: String, val time: Long)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val dailyBehaviorDao =
        AppDatabase.getDatabase(application).dailyBehaviorDao()

    private val today: LocalDate = LocalDate.now()

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

    var hasInitBySystemEvents = false

    init {
        initScheduleFromDbOrSystem()
    }

    /** ================= 初始化 ================= */

    private fun initScheduleFromDbOrSystem() {
        viewModelScope.launch {
            val entity = dailyBehaviorDao.getOrInitTodayBehavior(today)

            if (entity.wakeMinute == 0 && entity.sleepMinute == 0) {
                // 数据库没有 → 用系统推断
                val (sleep, wake) = getDefaultSleepWakeTime()

                val sleepCal = Calendar.getInstance().apply { timeInMillis = sleep }
                val wakeCal = Calendar.getInstance().apply { timeInMillis = wake }

                val sleepMinute = toMinuteOfDay(
                    sleepCal.get(Calendar.HOUR_OF_DAY),
                    sleepCal.get(Calendar.MINUTE)
                )
                val wakeMinute = toMinuteOfDay(
                    wakeCal.get(Calendar.HOUR_OF_DAY),
                    wakeCal.get(Calendar.MINUTE)
                )

                dailyBehaviorDao.updateSchedule(
                    date = today,
                    wakeMinute = wakeMinute,
                    sleepMinute = sleepMinute
                )

                applyScheduleToUI(
                    sleepCal.get(Calendar.HOUR_OF_DAY),
                    sleepCal.get(Calendar.MINUTE),
                    wakeCal.get(Calendar.HOUR_OF_DAY),
                    wakeCal.get(Calendar.MINUTE)
                )
            } else {
                // 数据库有 → 用数据库
                applyScheduleToUI(
                    sleepHour = entity.sleepMinute!! / 60,
                    sleepMinute = entity.sleepMinute % 60,
                    wakeHour = entity.wakeMinute!! / 60,
                    wakeMinute = entity.wakeMinute % 60
                )
            }

            hasInitBySystemEvents = true
        }
    }


    fun refreshBySystemEvents() {
        viewModelScope.launch {
            val entity = dailyBehaviorDao.getOrInitTodayBehavior(today)

            // 只要用户已经设置过，就绝不再用系统推断
            if (entity.wakeMinute!! < 1e-5 || entity.sleepMinute!! < 1e-5) {
                hasInitBySystemEvents = true
                return@launch
            }

            val (sleep, wake) = getDefaultSleepWakeTime()

            val sleepCal = Calendar.getInstance().apply { timeInMillis = sleep }
            val wakeCal = Calendar.getInstance().apply { timeInMillis = wake }

            val sleepMinute = toMinuteOfDay(
                sleepCal.get(Calendar.HOUR_OF_DAY),
                sleepCal.get(Calendar.MINUTE)
            )
            val wakeMinute = toMinuteOfDay(
                wakeCal.get(Calendar.HOUR_OF_DAY),
                wakeCal.get(Calendar.MINUTE)
            )

            dailyBehaviorDao.updateSchedule(
                date = today,
                wakeMinute = wakeMinute,
                sleepMinute = sleepMinute
            )

            applyScheduleToUI(
                sleepCal.get(Calendar.HOUR_OF_DAY),
                sleepCal.get(Calendar.MINUTE),
                wakeCal.get(Calendar.HOUR_OF_DAY),
                wakeCal.get(Calendar.MINUTE)
            )

            hasInitBySystemEvents = true
        }
    }



    /** ================= UI 同步 ================= */

    private fun applyScheduleToUI(
        sleepHour: Int,
        sleepMinute: Int,
        wakeHour: Int,
        wakeMinute: Int
    ) {
        onBedTimeSelected(
            String.format("%02d", sleepHour),
            String.format("%02d", sleepMinute),
            sleepHour,
            sleepMinute
        )

        onWakeTimeSelected(
            String.format("%02d", wakeHour),
            String.format("%02d", wakeMinute),
            wakeHour,
            wakeMinute
        )
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

    /** ================= 用户确认保存 ================= */

    fun saveScheduleToDb(
        bedHour: String,
        bedMinute: String,
        wakeHour: String,
        wakeMinute: String
    ) {
        viewModelScope.launch {
            val sleepMinuteOfDay = bedHour.toInt() * 60 + bedMinute.toInt()
            val wakeMinuteOfDay = wakeHour.toInt() * 60 + wakeMinute.toInt()

            dailyBehaviorDao.updateSchedule(
                date = today,
                wakeMinute = wakeMinuteOfDay,
                sleepMinute = sleepMinuteOfDay
            )
        }
    }


    /** ================= 工具 ================= */

    private fun toMinuteOfDay(hour: Int, minute: Int): Int {
        return hour * 60 + minute
    }

    private fun getScreenEventsToday(): List<ScreenEvent> {
        val usm = context.getSystemService(Application.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 4)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val events = mutableListOf<ScreenEvent>()
        val usageEvents = usm.queryEvents(cal.timeInMillis, System.currentTimeMillis())
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
}

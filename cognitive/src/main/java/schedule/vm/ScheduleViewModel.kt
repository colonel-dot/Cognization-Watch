package schedule.vm

import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.*

data class ScreenEvent(val type: String, val time: Long)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    // 1. 数据源
    val hours = (0..23).map { String.format("%02d", it) }
    val minutes = (0..59).map { String.format("%02d", it) }

    // 2. LiveData 用于绑定 UI
    private val _bedTimeText = MutableLiveData<String>()
    val bedTimeText: LiveData<String> = _bedTimeText

    private val _wakeTimeText = MutableLiveData<String>()
    val wakeTimeText: LiveData<String> = _wakeTimeText

    // 3. 保存滚轮选中位置
    var bedHourPos = 0
    var bedMinutePos = 0
    var wakeHourPos = 0
    var wakeMinutePos = 0

    init {
        // 初始化作息时间，优先使用屏幕事件
        val (defaultSleep, defaultWake) = getDefaultSleepWakeTime()
        val sleepHour = Calendar.getInstance().apply { timeInMillis = defaultSleep }.get(Calendar.HOUR_OF_DAY)
        val sleepMinute = Calendar.getInstance().apply { timeInMillis = defaultSleep }.get(Calendar.MINUTE)
        val wakeHour = Calendar.getInstance().apply { timeInMillis = defaultWake }.get(Calendar.HOUR_OF_DAY)
        val wakeMinute = Calendar.getInstance().apply { timeInMillis = defaultWake }.get(Calendar.MINUTE)

        // 设置初始值
        onBedTimeSelected(String.format("%02d", sleepHour), String.format("%02d", sleepMinute), sleepHour, sleepMinute)
        onWakeTimeSelected(String.format("%02d", wakeHour), String.format("%02d", wakeMinute), wakeHour, wakeMinute)
    }

    /** 用户手动选择睡觉时间 */
    fun onBedTimeSelected(hour: String, minute: String, hourPos: Int, minutePos: Int) {
        _bedTimeText.value = "睡觉时间: $hour:$minute"
        bedHourPos = hourPos
        bedMinutePos = minutePos
    }

    /** 用户手动选择起床时间 */
    fun onWakeTimeSelected(hour: String, minute: String, hourPos: Int, minutePos: Int) {
        _wakeTimeText.value = "起床时间: $hour:$minute"
        wakeHourPos = hourPos
        wakeMinutePos = minutePos
    }

    /** 使用 UsageStatsManager 获取最近一天屏幕事件（增强型） */
    private fun getScreenEventsToday(): List<ScreenEvent> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()

        // 今日 00:00
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        val end = System.currentTimeMillis()

        val eventsList = mutableListOf<ScreenEvent>()
        val usageEvents = usm.queryEvents(start, end)
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)

            when (event.eventType) {

                UsageEvents.Event.SCREEN_INTERACTIVE -> {
                    eventsList.add(ScreenEvent("SCREEN_ON", event.timeStamp))
                }
                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    eventsList.add(ScreenEvent("SCREEN_OFF", event.timeStamp))
                }
            }
        }

        return eventsList.sortedBy { it.time }
    }


    /**
     * 推算默认睡觉/起床时间（更稳健）：
     * - 优先在“夜间窗口”寻找最后一次 SCREEN_OFF（睡觉）
     * - 找到该睡觉点后，在其之后寻找第一个 SCREEN_ON（起床）
     * - 如果找不到夜间事件则使用全区间的 last SCREEN_OFF 与 first SCREEN_ON
     * - 最终仍无数据时使用 fallback 时间（23:00 / 07:00）
     */
    private fun getDefaultSleepWakeTime(): Pair<Long, Long> {
        val events = getScreenEventsToday()

        if (events.isEmpty()) {
            // 没权限或无事件 → 返回默认值
            val calendar = Calendar.getInstance()

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 0)
            val defaultSleep = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 7)
            calendar.set(Calendar.MINUTE, 0)
            val defaultWake = calendar.timeInMillis

            return defaultSleep to defaultWake
        }

        val firstOn = events.firstOrNull { it.type == "SCREEN_ON" }
        val lastOff = events.lastOrNull { it.type == "SCREEN_OFF" }

        val now = System.currentTimeMillis()

        return (lastOff?.time ?: now) to (firstOn?.time ?: now)
    }


    /** 时间格式化 */
    fun formatTime(time: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(time))
    }
}

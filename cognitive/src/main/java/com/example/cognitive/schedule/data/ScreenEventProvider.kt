package com.example.cognitive.schedule.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * 屏幕亮灭事件。
 */
data class ScreenEvent(val type: String, val time: Long)

/**
 * 屏幕事件数据源接口。
 * 将 UsageStatsManager 系统服务抽象为接口，使 ViewModel 可脱离 Android Framework 进行测试。
 */
interface ScreenEventProvider {
    suspend fun getScreenEventsToday(): List<ScreenEvent>
}

/**
 * 通过 [UsageStatsManager] 查询屏幕亮灭事件的实现。
 */
class UsageStatsScreenEventProvider(private val context: Context) : ScreenEventProvider {

    override suspend fun getScreenEventsToday(): List<ScreenEvent> =
        withContext(Dispatchers.IO) {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
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

            events.sortedBy { it.time }
        }
}

package com.example.cognitive.schedule.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.common.persistense.AppDatabase
import com.example.cognitive.repository.UpdateRepository
import com.example.cognitive.schedule.data.ScreenEvent
import com.example.cognitive.schedule.data.ScreenEventProvider
import com.example.cognitive.schedule.data.UsageStatsScreenEventProvider
import java.time.LocalDate
import java.util.*

private const val TAG = "ScheduleViewModel"

class ScheduleViewModel @JvmOverloads constructor(
    application: Application,
    private val screenEventProvider: ScreenEventProvider = UsageStatsScreenEventProvider(application)
) : AndroidViewModel(application) {

    private val dailyBehaviorDao =
        AppDatabase.getDatabase(application).dailyBehaviorDao()

    private val today: LocalDate = LocalDate.now()

    val hours = (0..23).map { String.format("%02d", it) }
    val minutes = (0..59).map { String.format("%02d", it) }

    private val _bedTimeText = MutableLiveData<String>()
    val bedTimeText: LiveData<String> = _bedTimeText

    private val _wakeTimeText = MutableLiveData<String>()
    val wakeTimeText: LiveData<String> = _wakeTimeText

    private val _scheduleHours = MutableLiveData<Double>()
    val scheduleHours: LiveData<Double> = _scheduleHours

    var bedHourPos = 0
    var bedMinutePos = 0
    var wakeHourPos = 0
    var wakeMinutePos = 0

    var hasInitBySystemEvents = false

    init {
        initScheduleFromDbOrSystem()
    }

    /**
     * 自定义 Factory，支持注入 [ScreenEventProvider]。
     * 用默认构造参数时无需显式传入；测试时注入 mock 即可。
     */
    class Factory @JvmOverloads constructor(
        private val application: Application,
        private val screenEventProvider: ScreenEventProvider = UsageStatsScreenEventProvider(application)
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScheduleViewModel(application, screenEventProvider) as T
        }
    }


    /* ================= 初始化 ================= */

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
                    sleepMinute = entity.sleepMinute!! % 60,
                    wakeHour = entity.wakeMinute!! / 60,
                    wakeMinute = entity.wakeMinute!! % 60
                )
            }

            hasInitBySystemEvents = true
        }
    }


    fun refreshBySystemEvents(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val entity = dailyBehaviorDao.getOrInitTodayBehavior(today)

            // 只要用户已经设置过，就从数据库读取而非用系统推断
            if (entity.wakeMinute!! > 1e-5 || entity.sleepMinute!! > 1e-5) {
                hasInitBySystemEvents = true
                applyScheduleToUI(
                    sleepHour = entity.sleepMinute!! / 60,
                    sleepMinute = entity.sleepMinute!! % 60,
                    wakeHour = entity.wakeMinute!! / 60,
                    wakeMinute = entity.wakeMinute!! % 60
                )
                onComplete?.invoke()
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
            onComplete?.invoke()
        }
    }


    /* ================= UI 同步 ================= */

    private fun applyScheduleToUI(
        sleepHour: Int,
        sleepMinute: Int,
        wakeHour: Int,
        wakeMinute: Int
    ) {
        val sleepMinuteOfDay = sleepHour * 60 + sleepMinute
        val wakeMinuteOfDay = wakeHour * 60 + wakeMinute

        // 计算睡眠时长
        val hours = if (wakeMinuteOfDay > sleepMinuteOfDay) {
            (wakeMinuteOfDay - sleepMinuteOfDay) / 60.0
        } else {
            (24 * 60 - sleepMinuteOfDay + wakeMinuteOfDay) / 60.0
        }
        _scheduleHours.value = hours

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
        _bedTimeText.value = "睡觉时间  $hour:$minute"
        bedHourPos = hourPos
        bedMinutePos = minutePos
    }

    fun onWakeTimeSelected(hour: String, minute: String, hourPos: Int, minutePos: Int) {
        _wakeTimeText.value = "起床时间  $hour:$minute"
        wakeHourPos = hourPos
        wakeMinutePos = minutePos
    }


    /* ================= 用户确认保存 ================= */

    fun saveScheduleToDb(
        bedHour: String,
        bedMinute: String,
        wakeHour: String,
        wakeMinute: String
    ) {
        viewModelScope.launch {
            val sleepMinuteOfDay = bedHour.toInt() * 60 + bedMinute.toInt()
            val wakeMinuteOfDay = wakeHour.toInt() * 60 + wakeMinute.toInt()

            UpdateRepository.updateScheduleTime(wakeMinuteOfDay, sleepMinuteOfDay)
            Log.d(TAG, "saveScheduleToDb: 已经调用了更新数据库的方法")
        }
    }


    /* ================= 工具 ================= */

    private fun toMinuteOfDay(hour: Int, minute: Int): Int {
        return hour * 60 + minute
    }

    /**
     * 通过 [ScreenEventProvider] 推断默认作息时间，替代原来直接调 UsageStatsManager。
     */
    private suspend fun getDefaultSleepWakeTime(): Pair<Long, Long> {
        val events = screenEventProvider.getScreenEventsToday()
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

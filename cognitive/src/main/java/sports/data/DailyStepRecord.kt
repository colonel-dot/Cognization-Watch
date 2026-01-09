package sports.data

data class DailyStepRecord(
    val date: String,        // 2025-12-05
    val stepCount: Int,      // 今日总步数
    val activeTimeSec: Int, // 今日运动秒数
    val restTimeSec: Int    // 今日休息秒数
)

package risk.preprocess

import persistense.DailyBehaviorEntity
import risk.model.NormalizedDailyBehavior

object BehaviorNormalizer {

    fun normalize(entity: DailyBehaviorEntity): NormalizedDailyBehavior {

        fun normalizeSleep(min: Int?): Int? {
            if (min == null || min == 0) return null

            // 以 4:00 作为一天起点
            // 0:00 - 3:59 认为是“前一天深夜” → +1440
            return if (min < 240) min + 1440 else min
        }

        fun normalizeWake(min: Int?): Int? {
            if (min == null || min == 0) return null

            return min
        }

        return NormalizedDailyBehavior(
            date = entity.date,

            wakeMinute = entity.wakeMinute?.takeIf { it > 0 },
            sleepMinute = normalizeSleep(entity.sleepMinute),

            schulte16Time = entity.schulte16TimeSec?.takeIf { it > 0.1 },
            schulte25Time = entity.schulte25TimeSec?.takeIf { it > 0.1 },

            speechScore = entity.speechScore?.takeIf { it > 0.1 },

            steps = entity.steps?.takeIf { it > 0 }
        )
    }
}

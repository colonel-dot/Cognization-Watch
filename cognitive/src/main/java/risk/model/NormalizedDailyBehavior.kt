package risk.model

import androidx.room.PrimaryKey
import persistense.DailyBehaviorEntity
import java.time.LocalDate

data class NormalizedDailyBehavior(
    val date: LocalDate,

    val wakeMinute: Int?,
    val sleepMinute: Int?,

    val schulte16Time: Double?,
    val schulte25Time: Double?,

    val speechScore: Double?,

    val steps: Int?
)

    fun DailyBehaviorEntity.toNormalized(): NormalizedDailyBehavior {
        return NormalizedDailyBehavior(
            date = this.date,
            wakeMinute = this.wakeMinute,
            sleepMinute = this.sleepMinute,
            schulte16Time = this.schulte16TimeSec,
            schulte25Time = this.schulte25TimeSec,
            speechScore = this.speechScore,
            steps = this.steps
        )
    }

    fun List<DailyBehaviorEntity>.toNormalizedList(): List<NormalizedDailyBehavior> {
        return this.map { it.toNormalized() }
    }



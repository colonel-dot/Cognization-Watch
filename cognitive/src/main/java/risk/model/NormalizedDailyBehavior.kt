package risk.model

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

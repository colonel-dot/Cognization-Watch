package risk.model

data class Baseline(
    val mean: Double,
    val std: Double,
    val median: Double,
    val q1: Double,//前25％
    val q3: Double//前75％
)

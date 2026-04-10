package collection.model

data class HealthMonitoringRVModel(
    val function: String?,
    val data: Double,
    val target: Double,
    val unit: String?
)
package risk.trend

object TrendDetector {

    fun theilSenSlope(values: List<Double>): Double {
        if (values.size < 5) return 0.0

        val slopes = mutableListOf<Double>()
        for (i in values.indices) {
            for (j in i + 1 until values.size) {
                slopes.add((values[j] - values[i]) / (j - i))
            }
        }
        return slopes.sorted()[slopes.size / 2]
    }

    fun schulteTrendRisk(values: List<Double>): Double {
        val slope = theilSenSlope(values)
        // 每天慢 3 秒以上 → 明显退化
        return when {
            slope < 1.5 -> 0.0
            slope < 3.0 -> 0.5
            else -> 1.0
        }
    }

    fun speechTrendRisk(values: List<Double>): Double {
        val slope = theilSenSlope(values)
        // 每天下降 0.3 分以上 → 风险
        return when {
            slope > -0.1 -> 0.0
            slope > -0.3 -> 0.5
            else -> 1.0
        }
    }
}

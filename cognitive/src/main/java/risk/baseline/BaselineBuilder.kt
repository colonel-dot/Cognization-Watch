package risk.baseline

import risk.model.Baseline
import kotlin.math.sqrt

object BaselineBuilder {

    fun build(values: List<Double>): Baseline? {
        if (values.size < 5) return null   // 少于5天不建模

        val sorted = values.sorted()
        val mean = sorted.average()
        val std = sqrt(sorted.map { (it - mean) * (it - mean) }.average())

        val q1 = sorted[sorted.size / 4]
        val q3 = sorted[sorted.size * 3 / 4]
        val median = sorted[sorted.size / 2]

        return Baseline(
            mean = mean,
            std = if (std < 1e-6) 1.0 else std, // 防止除0
            median = median,
            q1 = q1,
            q3 = q3
        )
    }
}

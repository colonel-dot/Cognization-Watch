package risk.anomaly

import risk.model.Baseline
import kotlin.math.abs

object AnomalyEngine {

    //Z-Score 异常（舒尔特 / 语音）
    fun zScoreAnomaly(x: Double, baseline: Baseline): Double {
        val z = abs(x - baseline.mean) / baseline.std
        return when {
            z < 1.5 -> 0.0
            z < 2.5 -> 0.5
            else -> 1.0
        }
    }

    //IQR 异常（步数 / 作息）
    fun iqrAnomaly(x: Double, baseline: Baseline): Double {
        val iqr = baseline.q3 - baseline.q1
        return if (x < baseline.q1 - 1.5 * iqr ||
            x > baseline.q3 + 1.5 * iqr) 1.0 else 0.0
    }

    // 昼夜节律异常
    fun rhythmAnomaly(values: List<Double>): Double {
        if (values.size < 5) return 0.0

        val mean = values.average()
        val varian = values.map { (it - mean) * (it - mean) }.average()

        // 方差 > 90分钟² 认为异常
        return if (varian > 90.0 * 90.0) 1.0 else 0.0
    }
}

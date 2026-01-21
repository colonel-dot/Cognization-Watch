package risk.work

import android.content.Context
import schulte.data.SchulteEvaluatorType

class RiskConfigManager(context: Context) {

    private val sp = context.getSharedPreferences(
        "risk_config",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_SCHULTE_TYPE = "schulte_type"
    }

    fun getSchulteEvaluatorType(): SchulteEvaluatorType {
        val name = sp.getString(
            KEY_SCHULTE_TYPE,
            SchulteEvaluatorType.GRID_5.name   // 默认 5×5
        ) ?: SchulteEvaluatorType.GRID_5.name

        return runCatching {
            SchulteEvaluatorType.valueOf(name)
        }.getOrElse {
            SchulteEvaluatorType.GRID_5   // 兜底防脏数据
        }
    }

    fun setSchulteEvaluatorType(type: SchulteEvaluatorType) {
        sp.edit()
            .putString(KEY_SCHULTE_TYPE, type.name)
            .apply()
    }
}

package risk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.cognitive.R
import risk.persistence.DailyRiskEntity
import risk.vm.RiskViewModel
import java.time.LocalDate

class RiskDetailBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val KEY_DATE = "date"

        fun newInstance(date: LocalDate): RiskDetailBottomSheet {
            val f = RiskDetailBottomSheet()
            val bundle = Bundle()
            bundle.putString(KEY_DATE, date.toString())
            f.arguments = bundle
            return f
        }
    }

    private lateinit var viewModel: RiskViewModel   // 下一步写

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_risk_detail, container, false)

        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvLevel = view.findViewById<TextView>(R.id.tvLevel)
        val tvScore = view.findViewById<TextView>(R.id.tvScore)
        val tvDetail = view.findViewById<TextView>(R.id.tvDetail)

        val dateStr = arguments?.getString(KEY_DATE) ?: return view
        val date = LocalDate.parse(dateStr)

        // 初始化 ViewModel
        viewModel = androidx.lifecycle.ViewModelProvider(this)[RiskViewModel::class.java]

        // 查询当天风险
        viewModel.queryRiskByDate(date)

        // 监听结果
        viewModel.riskData.observe(viewLifecycleOwner) { risk ->

            tvDate.text = "日期：$date"

            if (risk == null) {
                // 没有风险数据的核心兜底逻辑
                tvLevel.text = "暂无认知风险评估"
                tvScore.text = ""
                tvDetail.text = "该日期尚未生成风险分析（可能是首次打开或当天未完成评估）"
                return@observe
            }

            // 有数据时正常展示
            tvLevel.text = "风险等级：${risk.riskLevel}"
            tvScore.text = "总风险分：${"%.2f".format(risk.riskScore)}"

            tvDetail.text = """
                        睡眠风险：${"%.2f".format(risk.sleepRisk)}
                        舒尔特风险：${"%.2f".format(risk.schulteRisk)}
                        步数风险：${"%.2f".format(risk.stepsRisk)}
                        语音风险：${"%.2f".format(risk.speechRisk)}
                        
                        解释：
                        ${risk.explanations}
                                    """.trimIndent()
        }

        return view
    }
}

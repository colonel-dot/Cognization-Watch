package mine.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cognitive.R
import mine.vm.MineRecordViewModel
import read_assessment.vm.RecordViewModel
import kotlin.getValue

class MineRecordActivity : AppCompatActivity() {

    private val viewModel by viewModels<MineRecordViewModel>()
    private lateinit var read_record: TextView
    private lateinit var tvSchulteTime: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mine_record)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        read_record = findViewById<TextView>(R.id.read_record)
        tvSchulteTime = findViewById<TextView>(R.id.schulte_record)


        viewModel.queryTodayBehaviorData()

// 观察数据变化，数据回来后更新UI
        viewModel.todayBehaviorData.observe(this) { dailyData ->
            dailyData?.let {
                // 读取今日舒尔特最优成绩
                val schulteBestTime = it.schulteTimeSec ?: 0.0
                tvSchulteTime.text = "今日最优：${schulteBestTime / 1000}秒"

                // 读取今日语音评分
                val speechScore = it.speechScore ?: 0.0
                read_record.text = "语音评分：${speechScore}"

                // 读取今日步数
            } ?: run {
                // 当日无数据时的默认展示
                tvSchulteTime.text = "今日最优：暂无成绩"
            }
        }
    }
}
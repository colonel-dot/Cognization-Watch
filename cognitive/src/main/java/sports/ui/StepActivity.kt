package sports.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cognitive.R
import sports.vm.StepViewModel

class StepActivity : AppCompatActivity() {

    private val viewModel: StepViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step)

        val tvSteps = findViewById<TextView>(R.id.tvStepCount)
        val tvActive = findViewById<TextView>(R.id.tvActiveTime)
        val tvRest = findViewById<TextView>(R.id.tvRestTime)

        // 今日步数
        viewModel.stepCount.observe(this) { steps ->
            tvSteps.text = "今日步数：$steps"
        }

        // 今日运动时间（秒 → 分钟）
        viewModel.activeTime.observe(this) { activeSec ->
            val min = activeSec / 60
            tvActive.text = "今日运动：${min} 分钟"
        }

        // 今日休息时间（秒 → 分钟）
        viewModel.restTime.observe(this) { restSec ->
            val min = restSec / 60
            tvRest.text = "今日休息：${min} 分钟"
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startCounting()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopCounting()
    }
}

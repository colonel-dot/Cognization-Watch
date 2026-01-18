package sports.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cognitive.R
import sports.data.StepForegroundService
import sports.vm.StepViewModel

class StepActivity : AppCompatActivity() {

    private val viewModel: StepViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step)

        startService(Intent(this, StepForegroundService::class.java))

        val tvSteps = findViewById<TextView>(R.id.tvStepCount)
        val tvActive = findViewById<TextView>(R.id.tvActiveTime)
        val tvRest = findViewById<TextView>(R.id.tvRestTime)

        viewModel.stepCount.observe(this) {
            tvSteps.text = "今日步数：$it"
        }
        viewModel.activeTime.observe(this) {
            tvActive.text = "今日运动：${it / 60} 分钟"
        }
        viewModel.restTime.observe(this) {
            tvRest.text = "今日休息：${it / 60} 分钟"
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshToday()
    }
}

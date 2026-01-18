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

        viewModel.stepCount.observe(this) {
            tvSteps.text = "今日步数：$it"
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshToday()
    }
}

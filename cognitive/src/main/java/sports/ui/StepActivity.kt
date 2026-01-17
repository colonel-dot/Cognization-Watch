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

    private val vm: StepViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step)

        val tvActive = findViewById<TextView>(R.id.tvActiveTime)
        val tvRest = findViewById<TextView>(R.id.tvRestTime)

        vm.activeTime.observe(this) {
            tvActive.text = "今日运动：${it / 60} 分钟"
        }

        vm.restTime.observe(this) {
            tvRest.text = "今日休息：${it / 60} 分钟"
        }
    }

    override fun onResume() {
        super.onResume()
        startService(Intent(this, StepForegroundService::class.java))
    }
}

package com.example.cognitive

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import schulte.ui.SchulteGameActivity
import sports.data.StepForegroundService
import sports.vm.StepViewModel
import kotlin.getValue


private const val REQ_NOTIFY = 1001

class MainActivity : AppCompatActivity() {

    private val stepsViewModel: StepViewModel by viewModels()
    lateinit var mIntent: Intent
    lateinit var btn_game: View
    lateinit var btn_speak: View
    lateinit var btn_schedule: View
    lateinit var btn_mine: View
    lateinit var tvSteps:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (needNotificationPermission()) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                REQ_NOTIFY
            )
        } else {
            startStepService()
        }

        btn_mine = findViewById<View>(R.id.mine_layout)
        btn_game = findViewById<Button>(R.id.game_layout)
        btn_speak = findViewById<Button>(R.id.speak_layout)
        btn_schedule = findViewById<Button>(R.id.schedule_layout)
        tvSteps = findViewById<TextView>(R.id.tv_steps)
        btn_speak.setOnClickListener {
            mIntent = Intent(this, read_assessment.ui.RecordActivity::class.java)
            startActivity(mIntent) }
        btn_game.setOnClickListener {
            mIntent = Intent(this, SchulteGameActivity::class.java)
            startActivity(mIntent)
        }
        btn_schedule.setOnClickListener {
            mIntent = Intent(this, schedule.ui.ScheduleActivity::class.java)
            startActivity(mIntent)
        }
        btn_mine.setOnClickListener {
            mIntent = Intent(this, mine.ui.MineRecordActivity::class.java)
            startActivity(mIntent)
        }
        stepsViewModel.stepCount.observe(this) {
            tvSteps.text = "今日步数 $it"
        }
    }

    override fun onResume() {
        super.onResume()
        stepsViewModel.refreshToday()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        if (requestCode == REQ_NOTIFY) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startStepService()
            } else {
                Toast.makeText(this, "通知权限未授予，可能无法正常接收步数提醒", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------------- 工具方法 ----------------

    private fun needNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    }

    private fun startStepService() {
        val intent = Intent(this, StepForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 8.0+ 必须用这个启动前台服务
            startForegroundService(intent)
        } else {
            // 8.0以下用旧方法
            startService(intent)
        }
    }
}
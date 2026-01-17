package com.example.cognitive

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import schulte.ui.SchulteGameActivity

class MainActivity : AppCompatActivity() {
    lateinit var mIntent: Intent
    lateinit var btn_game: View
    lateinit var btn_speak: View
    lateinit var btn_schedule: View
    lateinit var btn_sports: View
    lateinit var btn_mine: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        

        btn_mine = findViewById<View>(R.id.mine_layout)
        btn_game = findViewById<Button>(R.id.game_layout)
        btn_speak = findViewById<Button>(R.id.speak_layout)
        btn_schedule = findViewById<Button>(R.id.schedule_layout)
        btn_sports = findViewById<View>(R.id.btn_sports)
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
        btn_sports.setOnClickListener {
            mIntent = Intent(this, sports.ui.StepActivity::class.java)
            startActivity(mIntent)
        }
        btn_mine.setOnClickListener {
            mIntent = Intent(this, mine.ui.MineRecordActivity::class.java)
            startActivity(mIntent)
        }
    }
}
package read_assessment.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cognitive.R
import read_assessment.vm.RecordViewModel
import com.github.squti.androidwaverecorder.WaveRecorder
import java.io.File


class RecordActivity : AppCompatActivity() {

    private val viewModel by viewModels<RecordViewModel>()

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        btnStart = findViewById<Button>(R.id.btn_start_record)
        btnStop = findViewById<Button>(R.id.btn_stop_record)
        val btnScore = findViewById<Button>(R.id.btn_score)
        val tvResult = findViewById<TextView>(R.id.tv_score)

        btnStart.setOnClickListener { checkPermissionAndStart() }
        btnStop.setOnClickListener { viewModel.stopRecord() }

        btnScore.setOnClickListener {
            viewModel.evaluateSpeech(
                refText = "我从去年辞帝京，谪居卧病浔阳城",
                langType = "zh-CHS"
            )
        }
        observeViewModel()
        viewModel.scoreResult.observe(this) {
            tvResult.text = it
        }
    }

    private fun observeViewModel() {
        viewModel.isRecording.observe(this) { isRec ->
            btnStart.isEnabled = !isRec
            btnStop.isEnabled = isRec
        }

        viewModel.recordSavedEvent.observe(this) { file ->
            if (file != null) {
                Toast.makeText(this, "录音已保存：$file", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkPermissionAndStart() {
        val perm = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), 1)
        } else {
            startRecord()
        }
    }

    private fun startRecord() {
        viewModel.startRecord(getExternalFilesDir("")!!)
        Toast.makeText(this, "开始录音...", Toast.LENGTH_SHORT).show()
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecord()
        } else {
            Toast.makeText(this, "没有录音权限", Toast.LENGTH_SHORT).show()
        }
    }
}

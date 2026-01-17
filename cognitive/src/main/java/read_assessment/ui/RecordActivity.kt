package read_assessment.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
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

private const val TAG = "RecordActivity"


class RecordActivity : AppCompatActivity() {

    private val viewModel by viewModels<RecordViewModel>()

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var imageChange: ImageView
    private lateinit var textChange: TextView
    private lateinit var tvResult: TextView
    private var speakText: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        val speakTextView = findViewById<TextView>(R.id.speak_text)
        btnStart = findViewById<Button>(R.id.btn_start_record)
        btnStop = findViewById<Button>(R.id.btn_stop_record)
        val btnScore = findViewById<Button>(R.id.btn_score)
        tvResult = findViewById<TextView>(R.id.tv_score)
        imageChange = findViewById<ImageView>(R.id.huanyige_img)
        textChange = findViewById<TextView>(R.id.huanyige_text)
        btnStart.setOnClickListener { checkPermissionAndStart() }
        btnStop.setOnClickListener { viewModel.stopRecord() }
        speakText = viewModel.getText()
        speakTextView.setText(speakText)
        imageChange.setOnClickListener { speakText = viewModel.getText()
            Log.d(TAG, "换过来的句子是:$speakText ")
            speakTextView.text = speakText
            Log.d(TAG, "TextView现在是：${speakTextView.text}")}
        textChange.setOnClickListener { speakText = viewModel.getText(); speakTextView.setText(speakText) }
        btnScore.setOnClickListener {
            viewModel.evaluateSpeech(
                refText = speakText,
                langType = "zh-CHS"
            )
        }
        observeViewModel()

    }

    private fun observeViewModel() {
        viewModel.isRecording.observe(this) { isRec ->
            btnStart.isEnabled = !isRec
            btnStop.isEnabled = isRec
        }

        viewModel.scoreResult.observe(this) {
            tvResult.text = "$it"
        }

        viewModel.recordSavedEvent.observe(this) { file ->
            if (file != null) {
                Toast.makeText(this, "录音已保存：$file", Toast.LENGTH_SHORT).show()
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

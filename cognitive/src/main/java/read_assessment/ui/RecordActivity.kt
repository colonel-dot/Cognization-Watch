package read_assessment.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cognitive.R
import com.github.squti.androidwaverecorder.WaveRecorder
import java.io.File

class RecordActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RecordActivity"
        private const val REQ_RECORD_AUDIO = 1
    }

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    private var recorder: WaveRecorder? = null
    private lateinit var outputFile: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_record)

        btnStart = findViewById(R.id.btn_start_record)
        btnStop = findViewById(R.id.btn_stop_record)

        btnStop.isEnabled = false

        btnStart.setOnClickListener { checkPermissionAndStart() }
        btnStop.setOnClickListener { stopRecord() }
    }

    private fun checkPermissionAndStart() {//检查权限
        val neededPermission = Manifest.permission.RECORD_AUDIO

        if (ContextCompat.checkSelfPermission(this, neededPermission)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(neededPermission),
                REQ_RECORD_AUDIO
            )
        } else {
            startRecord()
        }
    }

    override fun onRequestPermissionsResult(//处理权限
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_RECORD_AUDIO &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecord()
        } else {
            Toast.makeText(this, "未授予录音权限，无法录音", Toast.LENGTH_SHORT).show()
        }
    }

    // -------------------------------
    // 录音实现
    // -------------------------------
    private fun startRecord() {
        // 目录：Android/data/包名/files/recordings/
        val dir = getExternalFilesDir("recordings")!!
        if (!dir.exists()) dir.mkdirs()

        outputFile = File(dir, "record_${System.currentTimeMillis()}.wav").absolutePath

        recorder = WaveRecorder(outputFile).apply {
            noiseSuppressorActive = true

            // 使用新的配置方法替代 waveConfig
            configureWaveSettings {
                sampleRate = 16000      // 16kHz 采样率
                channels = AudioFormat.CHANNEL_IN_MONO            // 单声道
                audioEncoding = AudioFormat.ENCODING_PCM_16BIT      // 16bit 位深
            }
        }

        recorder?.startRecording()

        btnStart.isEnabled = false
        btnStop.isEnabled = true

        Toast.makeText(this, "开始录音...", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Recording to: $outputFile")
    }

    private fun stopRecord() {
        recorder?.stopRecording()
        recorder = null

        btnStart.isEnabled = true
        btnStop.isEnabled = false

        Toast.makeText(this, "录音已保存：$outputFile", Toast.LENGTH_LONG).show()
        Log.d(TAG, "Saved file: $outputFile")
    }

    // -------------------------------
    // 生命周期：释放资源
    // -------------------------------
    override fun onDestroy() {
        recorder?.stopRecording()
        recorder = null
        super.onDestroy()
    }
}

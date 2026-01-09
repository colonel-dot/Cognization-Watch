package read_assessment.vm

import android.content.Context
import android.media.AudioFormat
import android.util.Log
import androidx.lifecycle.*
import com.github.squti.androidwaverecorder.WaveRecorder
import kotlinx.coroutines.launch
import read_assessment.data.ReadAssessmentRepository
import read_assessment.data.ReadAssessmentSource
import java.io.File
import kotlin.random.Random

private const val TAG = "RecordViewModel"

class RecordViewModel : ViewModel() {

    private val repo = ReadAssessmentRepository()

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    private val _recordSavedEvent = MutableLiveData<File?>()
    val recordSavedEvent: LiveData<File?> = _recordSavedEvent

    private val _scoreResult = MutableLiveData<String>()
    val scoreResult: LiveData<String> = _scoreResult

    private var recorder: WaveRecorder? = null
    private var curFile: File? = null

    fun getText(): String {
        val cnt = Random.nextInt(0, 100)
        val speakText: String = ReadAssessmentSource.getTextByIndex(cnt)
        return speakText
    }


    /** 开始录音 */
    fun startRecord(rootDir: File) {
        val dir = File(rootDir, "recordings")
        if (!dir.exists()) dir.mkdirs()

        curFile = File(dir, "record_${System.currentTimeMillis()}.wav")

        recorder = WaveRecorder(curFile!!.absolutePath).apply {
            noiseSuppressorActive = true
            configureWaveSettings {
                sampleRate = 16000
                channels = AudioFormat.CHANNEL_IN_MONO
                audioEncoding = AudioFormat.ENCODING_PCM_16BIT
            }
        }

        recorder?.startRecording()
        _isRecording.value = true
    }

    fun stopRecord() {
        recorder?.stopRecording()
        _isRecording.postValue(false)
        _recordSavedEvent.postValue(curFile)
        recorder = null
    }

    fun evaluateSpeech(refText: String, langType: String) {
        val file = curFile ?: return

        viewModelScope.launch {
            val json = repo.evaluate(file, refText, langType)

            val overall = json.optDouble("overall", 0.0)
            val fluency = json.optDouble("fluency", 0.0)
            val integrity = json.optDouble("integrity", 0.0)
            val pronunciation = json.optDouble("pronunciation", 0.0)

            val result = """
                综合评分：$overall
                完整度：$integrity
                流利度：$fluency
                准确度：$pronunciation
            """.trimIndent()

            _scoreResult.postValue(result)
        }
    }
}

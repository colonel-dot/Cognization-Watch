package com.example.cognitive.read_assessment.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.common.persistense.AppDatabase
import com.example.cognitive.read_assessment.data.AudioRecorderManager
import com.example.cognitive.read_assessment.data.ReadAssessmentRepository
import com.example.cognitive.read_assessment.data.ReadAssessmentSource
import com.example.cognitive.repository.UpdateRepository
import java.io.File
import java.time.LocalDate
import kotlin.random.Random

private const val TAG = "RecordViewModel"

class ReadViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ReadAssessmentRepository()
    private val audioManager = AudioRecorderManager()

    private val appDatabase = AppDatabase.getDatabase(application)
    private val dailyBehaviorDao = appDatabase.dailyBehaviorDao()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    /** 一次性事件：录音文件保存结果。用 SharedFlow 避免粘性问题。 */
    private val _recordResult = MutableSharedFlow<File>(extraBufferCapacity = 1)
    val recordResult: SharedFlow<File> = _recordResult.asSharedFlow()

    private val _scoreResult = MutableStateFlow("")
    val scoreResult: StateFlow<String> = _scoreResult.asStateFlow()

    fun getText(): String {
        val cnt = Random.nextInt(0, 100)
        return ReadAssessmentSource.getTextByIndex(cnt)
    }

    fun startRecord(rootDir: File) {
        audioManager.start(rootDir)
        _isRecording.value = true
    }

    fun stopRecord() {
        val file = audioManager.stop()
        _isRecording.value = false
        if (file != null) {
            _recordResult.tryEmit(file)
        }
    }

    fun evaluateSpeech(file: File, refText: String, langType: String) {
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

            saveRecordToDatabase(overall)

            _scoreResult.value = result
        }
    }

    private suspend fun saveRecordToDatabase(score: Double) {
        val today = LocalDate.now()
        dailyBehaviorDao.getOrInitTodayBehavior(today)
        UpdateRepository.updateSpeechScore(score)
    }
}

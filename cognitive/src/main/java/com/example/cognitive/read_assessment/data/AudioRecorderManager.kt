package com.example.cognitive.read_assessment.data

import android.media.AudioFormat
import com.github.squti.androidwaverecorder.WaveRecorder
import java.io.File

/**
 * 封装 WaveRecorder 硬件操作，将音频录制逻辑与 ViewModel 解耦。
 * ViewModel 只关心"开始/停止录音"的语义，不关心录音器的具体配置。
 */
class AudioRecorderManager {

    private var recorder: WaveRecorder? = null
    private var currentFile: File? = null

    /**
     * 开始录音。
     * @param rootDir 外部存储根目录，录音文件将保存在 rootDir/recordings/ 下
     * @return 创建的录音文件
     */
    fun start(rootDir: File): File {
        val dir = File(rootDir, "recordings")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "record_${System.currentTimeMillis()}.wav")
        currentFile = file

        recorder = WaveRecorder(file.absolutePath).apply {
            noiseSuppressorActive = true
            configureWaveSettings {
                sampleRate = 16000
                channels = AudioFormat.CHANNEL_IN_MONO
                audioEncoding = AudioFormat.ENCODING_PCM_16BIT
            }
        }

        recorder?.startRecording()
        return file
    }

    /**
     * 停止录音。
     * @return 录音文件；如果未在录音则返回 null
     */
    fun stop(): File? {
        recorder?.stopRecording()
        recorder = null
        val file = currentFile
        currentFile = null
        return file
    }

    /**
     * 是否正在录音。
     */
    fun isRecording(): Boolean = recorder != null
}

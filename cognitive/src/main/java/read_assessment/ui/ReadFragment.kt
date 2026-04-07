package read_assessment.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.cognitive.R
import com.example.cognitive.main.MainViewModel
import read_assessment.vm.ReadViewModel
import java.io.File

class ReadFragment : Fragment() {
    private lateinit var bar_1: View
    private lateinit var bar_2: View
    private lateinit var bar_3: View
    private lateinit var bar_4: View
    private lateinit var bar_5: View
    // 修复1：存储所有属性动画实例，用于后续停止
    private val voiceAnimators = mutableListOf<ObjectAnimator>()

    // 修复2：viewModels 委托无需显式指定泛型（Kotlin自动推导），且移除多余的!!
    private val viewModel: ReadViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels<MainViewModel>()

    // 修复3：控件用 lateinit 替代可空，避免!!断言
    private lateinit var read: TextView
    private lateinit var mic: ImageView
    private lateinit var stop: TextView
    private lateinit var result: TextView

    private var speakText: String? = null

    // 修复4：修正权限申请泛型（去掉可空，避免类型不匹配）
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startRecord()
            startAllVoiceAnimation() // 权限授予后立即启动动画
        } else {
            Toast.makeText(requireContext(), "没有录音权限，无法录音", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_read, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)
        observeViewModel()
        bindClickListener()
    }

    private fun bindView(view: View) {
        // 修复5：去掉可空，直接赋值（lateinit保证初始化）
        read = view.findViewById(R.id.read)
        mic = view.findViewById(R.id.mic)
        stop = view.findViewById(R.id.stop)
        result = view.findViewById(R.id.result)

        // 修复6：安全获取文本，避免空指针
        speakText = viewModel.getText() ?: "默认朗读文本"
        read.text = speakText

        bar_1 = view.findViewById(R.id.bar_1)
        bar_2 = view.findViewById(R.id.bar_2)
        bar_3 = view.findViewById(R.id.bar_3)
        bar_4 = view.findViewById(R.id.bar_4)
        bar_5 = view.findViewById(R.id.bar_5)
    }

    // 启动所有属性动画（保存实例到列表）
    fun startAllVoiceAnimation() {
        // 先清空旧动画，避免重复启动
        stopAllVoiceAnimation()
        voiceAnimators.clear()

        startVoiceAnim(bar_1, 0)
        startVoiceAnim(bar_2, 120)
        startVoiceAnim(bar_3, 240)
        startVoiceAnim(bar_4, 120)
        startVoiceAnim(bar_5, 0)
    }

    // 正确停止属性动画（取消动画实例+清空列表）
    fun stopAllVoiceAnimation() {
        voiceAnimators.forEach { animator ->
            animator.cancel() // 终止属性动画运行
            animator.removeAllUpdateListeners() // 移除监听，避免内存泄漏
        }
        voiceAnimators.clear()

        // 额外：重置View的scaleY属性，避免停在动画中间状态
        bar_1.scaleY = 1f
        bar_2.scaleY = 1f
        bar_3.scaleY = 1f
        bar_4.scaleY = 1f
        bar_5.scaleY = 1f
    }

    private fun startVoiceAnim(bar: View, delay: Int) {
        val animator = ObjectAnimator.ofFloat(bar, "scaleY", 0.4f, 1f).apply {
            duration = 500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            startDelay = delay.toLong()
        }
        animator.start()
        // 保存动画实例到列表
        voiceAnimators.add(animator)
    }

    fun bindClickListener() {
        read.setOnClickListener {
            speakText = viewModel.getText() ?: "默认朗读文本"
            Log.d(TAG, "换过来的句子是: $speakText")
            read.text = speakText
            Log.d(TAG, "TextView 现在是: ${read.text}")
        }

        mic.setOnClickListener {
            if (checkPermissions()) {
                startRecord()
                startAllVoiceAnimation() // 启动录音 + 动画
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        stop.setOnClickListener {
            viewModel.stopRecord()
            stopAllVoiceAnimation() // 停止录音 + 动画
            // 停止后自动触发评估
            speakText?.let { text ->
                result.text = "评估中..."
                viewModel.evaluateSpeech(text, "zh-CHS")
                mainViewModel.notifyRecordChanged()
            } ?: run {
                result.text = "暂无朗读文本"
            }
        }

        // result 不再需要点击触发评估，评估结果会通过 observe 自动更新
    }

    private fun observeViewModel() {
        viewModel.isRecording.observe(viewLifecycleOwner) { isRec ->
            mic.isEnabled = !isRec
            stop.isEnabled = isRec
        }

        viewModel.scoreResult.observe(viewLifecycleOwner) { score ->
            result.text = score ?: "暂无评分"
        }

        viewModel.recordSavedEvent.observe(viewLifecycleOwner) { file ->
            file?.let {
                Toast.makeText(requireContext(), "录音已保存：${it.absolutePath}", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(requireContext(), "录音保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startRecord() {
        val dir = requireContext().getExternalFilesDir(null)
        if (dir != null) {
            viewModel.startRecord(dir)
            Toast.makeText(requireContext(), "开始录音", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "存储目录获取失败，无法录音", Toast.LENGTH_SHORT).show()
        }
    }

    // 修复10：Fragment销毁时停止所有动画，避免内存泄漏
    override fun onDestroyView() {
        super.onDestroyView()
        stopAllVoiceAnimation()
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        fun newInstance(param1: String? = null, param2: String? = null): ReadFragment {
            val fragment = ReadFragment()
            val args = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
            fragment.arguments = args
            return fragment
        }

        private const val TAG = "ReadFragment"
    }
}
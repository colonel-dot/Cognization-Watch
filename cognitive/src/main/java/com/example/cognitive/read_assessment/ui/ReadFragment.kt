package com.example.cognitive.read_assessment.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cognitive.R
import com.example.cognitive.databinding.FragmentReadBinding
import com.example.cognitive.main.MainViewModel
import com.example.cognitive.read_assessment.vm.ReadViewModel
import kotlinx.coroutines.launch

class ReadFragment : Fragment() {
    private var _binding: FragmentReadBinding? = null
    private val binding get() = _binding!!

    private val voiceAnimators = mutableListOf<ObjectAnimator>()

    private val viewModel: ReadViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels<MainViewModel>()

    private var speakText: String? = null

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
    ): View {
        _binding = FragmentReadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeViewModel()
        bindClickListener()
    }

    private fun initView() {
        speakText = viewModel.getText()
        binding.read.text = speakText
    }

    fun startAllVoiceAnimation() {
        stopAllVoiceAnimation()
        voiceAnimators.clear()

        startVoiceAnim(binding.bar1, 0)
        startVoiceAnim(binding.bar2, 120)
        startVoiceAnim(binding.bar3, 240)
        startVoiceAnim(binding.bar4, 120)
        startVoiceAnim(binding.bar5, 0)
    }

    fun stopAllVoiceAnimation() {
        voiceAnimators.forEach { animator ->
            animator.cancel()
            animator.removeAllUpdateListeners()
        }
        voiceAnimators.clear()

        binding.bar1.scaleY = 1f
        binding.bar2.scaleY = 1f
        binding.bar3.scaleY = 1f
        binding.bar4.scaleY = 1f
        binding.bar5.scaleY = 1f
    }

    private fun startVoiceAnim(bar: View, delay: Int) {
        val animator = ObjectAnimator.ofFloat(bar, "scaleY", 0.4f, 1f).apply {
            duration = 500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            startDelay = delay.toLong()
        }
        animator.start()
        voiceAnimators.add(animator)
    }

    fun bindClickListener() {
        binding.read.setOnClickListener {
            speakText = viewModel.getText()
            Log.d(TAG, "换过来的句子是: $speakText")
            binding.read.text = speakText
            Log.d(TAG, "TextView 现在是: ${binding.read.text}")
        }

        binding.mic.setOnClickListener {
            if (checkPermissions()) {
                startRecord()
                startAllVoiceAnimation()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        binding.stop.setOnClickListener {
            viewModel.stopRecord()
            stopAllVoiceAnimation()
        }
    }

    private fun observeViewModel() {
        // 持续状态：使用 StateFlow + repeatOnLifecycle 观察
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isRecording.collect { isRec ->
                        binding.mic.isEnabled = !isRec
                        binding.stop.isEnabled = isRec
                    }
                }
                launch {
                    viewModel.scoreResult.collect { score ->
                        binding.result.text = score.ifEmpty { "暂无评分" }
                    }
                }
            }
        }

        // 一次性事件：用 SharedFlow 替代 LiveData，消除粘性事件 bug
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recordResult.collect { file ->
                    Toast.makeText(requireContext(), "录音已保存", Toast.LENGTH_SHORT).show()
                    binding.result.text = "评估中..."
                    val text = speakText ?: return@collect
                    viewModel.evaluateSpeech(file, text, "zh-CHS")
                    mainViewModel.notifyRecordChanged()
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        stopAllVoiceAnimation()
        _binding = null
    }

    companion object {
        private const val TAG = "ReadFragment"
    }
}
package com.example.common.bind_device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.common.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class BindDialogFragment : DialogFragment() {

    private lateinit var btnBind: MaterialButton
    private lateinit var etBind: EditText

    // 使用 activityViewModels() 可以在 Activity 和 Dialog 之间共享数据
    // 如果只需要 Dialog 自己用，就用 viewModels()
    private val viewModel: BindViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val params = window.attributes
            val widthInPx = (320 * resources.displayMetrics.density).toInt()
            params.width = widthInPx
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 使用你之前重绘的 dialog_bind.xml
        return inflater.inflate(R.layout.dialog_fragment_bind, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置背景透明（如果你在 XML 里用了 MaterialCardView 的圆角）
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnBind = view.findViewById(R.id.bind_button)
        etBind = view.findViewById(R.id.bind_username)

        if (::btnBind.isInitialized) btnBind.setOnClickListener {
            val bindName = etBind.text.toString().trim()
            if (bindName.isBlank()) {
                Toast.makeText(context, "请输入绑定用户名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.bind(bindName)
        }

        observeBindAndLoadState()
    }

    private fun observeBindAndLoadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 注意：Fragment 中使用 viewLifecycleOwner
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.bindAndLoadState.collect { state ->
                    when (state) {
                        is BindAndLoadState.BindSuccess -> {
                            Toast.makeText(context, "绑定成功，正在加载数据...", Toast.LENGTH_SHORT).show()
                        }
                        is BindAndLoadState.DataLoadSuccess -> {
                            Toast.makeText(context, "绑定并加载数据成功", Toast.LENGTH_SHORT).show()
                            dismiss() // 成功后关闭弹窗
                        }
                        is BindAndLoadState.DataLoadFailure -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }
                        is BindAndLoadState.BindFailure -> {
                            Toast.makeText(context, "绑定失败", Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
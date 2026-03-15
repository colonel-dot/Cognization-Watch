package bind_device

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cognitive.R
import kotlinx.coroutines.launch

private const val TAG = "BindActivity"
class BindActivity : AppCompatActivity() {
    lateinit var btn_bind: View
    lateinit var tv_bind: EditText
    private val viewModel: BindViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bind)
        btn_bind = findViewById<Button>(R.id.bind_button)
        tv_bind = findViewById<EditText>(R.id.bind_username)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 绑定按钮点击事件
        btn_bind.setOnClickListener {
            val bindName = tv_bind.text.toString().trim()
            // 空值校验：避免传入空字符串
            if (bindName.isBlank()) {
                Toast.makeText(this, "请输入绑定用户名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.bind(bindName)
        }

        // 监听绑定+数据加载状态（替换原来的 checkBindResult）
        observeBindAndLoadState()

        // 移除无用的 bind() 方法（原来的逻辑已移到 ViewModel 中，Activity 无需重复写）
    }

    // 新增：监听 ViewModel 的密封类状态
    private fun observeBindAndLoadState() {
        lifecycleScope.launch {
            // 绑定生命周期：仅在页面前台时监听，避免后台消耗
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.bindAndLoadState.collect { state ->
                    when (state) {
                        // 绑定成功
                        is BindAndLoadState.BindSuccess -> {
                            Toast.makeText(this@BindActivity, "绑定成功，正在加载数据...", Toast.LENGTH_SHORT).show()
                        }
                        // 数据加载中（可选：显示加载框）
                        is BindAndLoadState.DataLoading -> {
                            // 示例：显示加载框（需自己实现 LoadingDialog）
                            // showLoadingDialog()
                        }
                        // 数据加载成功
                        is BindAndLoadState.DataLoadSuccess -> {
                            // 隐藏加载框
                            // dismissLoadingDialog()
                            Toast.makeText(this@BindActivity, "绑定并加载数据成功", Toast.LENGTH_SHORT).show()
                            finish() // 绑定+加载完成，关闭页面
                        }
                        // 数据加载失败
                        is BindAndLoadState.DataLoadFailure -> {
                            // dismissLoadingDialog()
                            Toast.makeText(this@BindActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        // 绑定失败
                        is BindAndLoadState.BindFailure -> {
                            Toast.makeText(this@BindActivity, "绑定失败，请重试", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    // 移除原来的 bind() 方法（已移到 ViewModel，Activity 只负责触发和监听，不处理业务逻辑）
    // 移除原来的 checkBindResult() 方法（被 observeBindAndLoadState 替代）
}
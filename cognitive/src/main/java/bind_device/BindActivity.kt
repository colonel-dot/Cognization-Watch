package bind_device

import android.content.Context
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
import androidx.lifecycle.lifecycleScope
import com.example.cognitive.R
import debug_login.LoginStatusManager
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
        //绑定按钮，未绑定时绑定
        btn_bind.setOnClickListener {
            viewModel.bind(tv_bind.text.toString())
        }
        checkBindResult()


    }



    private fun bind(context: Context) {
        val bindname = tv_bind.text.toString()

        lifecycleScope.launch {
            val mname = context.getSharedPreferences("login_status", Context.MODE_PRIVATE).getString("username", "") ?: ""
            try {
                BindRepository().bind(BindRequest(mname, bindname)).collect { result ->
                    result.fold(
                        onSuccess = { bindResponse ->
                            if (bindResponse.code == 200) {
                                BindStatusManager.saveBindStatus(context, true, bindname)
                                Log.d(TAG, "bind: 绑定成功了")
                                Toast.makeText(this@BindActivity, "绑定成功", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        },
                        onFailure = { e ->
                            Log.d(TAG, "bind:${e.message}")
                            Toast.makeText(this@BindActivity, "网络请求失败", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } catch (e: Exception) {
                Log.d(TAG, "bind: $e")
            }
        }
    }

    private fun checkBindResult() {
        lifecycleScope.launch {
            viewModel.bindResult.collect { isSuccess ->
                // 布尔值用 == 判断（或直接用 if）
                if (isSuccess) {
                    Toast.makeText(this@BindActivity, "绑定成功", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@BindActivity, "绑定失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
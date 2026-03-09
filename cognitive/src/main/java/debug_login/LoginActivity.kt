package debug_login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cognitive.R
import com.example.cognitive.main.ConMainActivity
import kotlinx.coroutines.launch

private const val TAG = "LoginActivity"
private const val INDENTITY_ELDER = "elder"
private const val INDENTITY_CHILD = "child"

class LoginActivity : AppCompatActivity() {

    private lateinit var btn_login: Button
    private lateinit var btn_child: View
    private lateinit var btn_elder: View
    private lateinit var account_login: EditText
    private lateinit var password_login: EditText
    private lateinit var identity: String
    private var isChoseIdentity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        account_login = findViewById<EditText>(R.id.login_account)
        password_login = findViewById<EditText>(R.id.login_password)
        btn_login = findViewById<Button>(R.id.login_button)
        btn_child = findViewById<TextView>(R.id.child)
        btn_elder = findViewById<TextView>(R.id.elder)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        login()
    }

    private fun initIdentity() {
        btn_child.setOnClickListener {
            isChoseIdentity = true
            identity = INDENTITY_CHILD
        }
        btn_elder.setOnClickListener {
            isChoseIdentity = true
            identity = INDENTITY_ELDER
        }
    }

    private fun login() {
        if (!isChoseIdentity) {
            Toast.makeText(this, "请选择身份", Toast.LENGTH_SHORT).show()
            return
        }
        btn_login.setOnClickListener {
            val userName = account_login.text.toString()
            val password = password_login.text.toString()
            lifecycleScope.launch {
                LoginRepository().login(userName, password).collect { result ->
                    result.fold(
                        onSuccess = { loginResponse ->
                            Log.d(TAG, "login: 收到了账号密码分别是${userName}和${password}的登录请求，服务器返回了状态码${loginResponse.code}")
                            // 请求成功，判断状态码
                            if (loginResponse.code == 200) {
                                Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                                LoginStatusManager.saveLoginStatus(applicationContext, true, userName, identity)
                                val intent = Intent(this@LoginActivity, ConMainActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@LoginActivity, "登录失败：账号或密码错误", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onFailure = { e ->
                            Log.d(TAG, "login: 收到了账号密码分别是${userName}和${password}的登录请求但是失败了")
                            // 请求失败（网络错误、解析错误等）
                            Toast.makeText(this@LoginActivity, "请求失败：${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }


}
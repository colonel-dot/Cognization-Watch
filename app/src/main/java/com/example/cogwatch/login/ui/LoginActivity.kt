package com.example.cogwatch.login.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.core.content.ContextCompat
import android.content.res.ColorStateList
import com.example.cogwatch.R
import com.example.cogwatch.login.ui.view.TypewriterTextView
import com.example.cogwatch.login.remote.LoginRepository
import com.example.common.bind_device.BindStatusManager
import com.example.common.login.remote.LoginStatusManager
import com.example.bridge.main.ChildrenActivity
import kotlinx.coroutines.launch
import com.example.cognitive.main.MainActivity
import com.example.cogwatch.login.validator.InputValidator

private const val TAG = "LoginActivity"
private const val IDENTITY_ELDER = "elder"
private const val IDENTITY_CHILD = "child"

class LoginActivity : AppCompatActivity() {

    private lateinit var title: TypewriterTextView
    private lateinit var elder: TextView
    private lateinit var children: TextView
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var login: Button
    private lateinit var guestLogin: TextView

    private var identity: String = ""
    private var isChoseIdentity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindView()
        elder.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
        children.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
        bindClickListener()
        title.animateText("怎么称呼您?")
    }

    private fun bindView() {
        title = findViewById(R.id.title)
        elder = findViewById(R.id.elder)
        children = findViewById(R.id.children)
        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        login = findViewById(R.id.login)
        guestLogin = findViewById(R.id.guest_login)
    }

    private fun bindClickListener() {
        elder.setOnClickListener {
            isChoseIdentity = true
            identity = IDENTITY_ELDER
            elder.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue))
            children.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
            Log.d(TAG, "onCreate: 点击选择老人端")
        }

        children.setOnClickListener {
            isChoseIdentity = true
            identity = IDENTITY_CHILD
            children.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue))
            elder.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
            Log.d(TAG, "onCreate: 点击选择子女端")
        }

        login.setOnClickListener {
            // TODO: 假作真时真亦假
            // login()
            guestLogin()
        }

        guestLogin.setOnClickListener {
            guestLogin()
        }
    }

    private fun login() {
        if (!isChoseIdentity) {
            Toast.makeText(this, "请选择身份", Toast.LENGTH_SHORT).show()
            return
        }
        val userName = username.text.toString()
        val passWord = password.text.toString()

        if (!InputValidator.isUsernameValid(userName)) {
            Toast.makeText(this, "账号格式不正确（支持手机号/邮箱/3-20位字母数字）", Toast.LENGTH_SHORT).show()
            return
        }

        if (!InputValidator.isPasswordValid(passWord)) {
            val strengthLevel = InputValidator.getPasswordStrengthLevel(passWord)
            val strengthText = InputValidator.getPasswordStrengthText(strengthLevel)
            Toast.makeText(this, "密码不符合要求（当前强度：$strengthText）", Toast.LENGTH_SHORT).show()
            return
        }

        if (InputValidator.hasExcessiveRepeatingChars(passWord)) {
            Toast.makeText(this, "密码不允许超过5位连续重复字符", Toast.LENGTH_SHORT).show()
            return
        }

        if (InputValidator.hasKeyboardSequence(passWord)) {
            Toast.makeText(this, "密码不允许使用键盘连续序列", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            LoginRepository().login(userName, passWord).collect { result ->
                result.fold(
                    onSuccess = { loginResponse ->
                        Log.d(TAG, "login: 收到账号密码分别是 $userName 和 $passWord 的登录请求，服务器返回状态码 ${loginResponse.code}")
                        if (loginResponse.code == 200) {
                            Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                            LoginStatusManager.saveLoginStatus(applicationContext, true, userName, identity)
                            val intent = if (identity == IDENTITY_ELDER) {
                                Intent(this@LoginActivity, MainActivity::class.java)
                            } else {
                                Intent(this@LoginActivity, ChildrenActivity::class.java)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "登录失败：账号或密码错误", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { e ->
                        Log.d(TAG, "login: 收到账号密码分别是 $userName 和 $passWord 的登录请求但是失败")
                        Toast.makeText(this@LoginActivity, "请求失败：${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun guestLogin() {
        if (!isChoseIdentity) {
            Toast.makeText(this, "请选择身份", Toast.LENGTH_SHORT).show()
            return
        }

        if (identity == IDENTITY_CHILD) {
            LoginStatusManager.saveLoginStatus(applicationContext, true, "children", identity)
            BindStatusManager.saveBindStatus(this, true, "elder", "妈妈")
        } else {
            LoginStatusManager.saveLoginStatus(applicationContext, true, "elder", identity)
            BindStatusManager.saveBindStatus(this, true, "children", "儿子")
        }

        val intent = if (identity == IDENTITY_ELDER) {
            Intent(this@LoginActivity, MainActivity::class.java)
        } else {
            Intent(this@LoginActivity, ChildrenActivity::class.java)
        }
        intent.putExtra("guest", true)
        startActivity(intent)
        finish()
    }
}
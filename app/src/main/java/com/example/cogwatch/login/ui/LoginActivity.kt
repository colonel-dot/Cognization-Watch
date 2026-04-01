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
import com.example.common.login.remote.LoginStatusManager
import com.example.bridge.main.ChildrenActivity
import kotlinx.coroutines.launch
import main.MainActivity

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
            fakelogin()
            // login()
        }
    }

    private fun login() {
        if (!isChoseIdentity) {
            Toast.makeText(this, "请选择身份", Toast.LENGTH_SHORT).show()
            return
        }
        val userName = username.text.toString()
        val passWord = password.text.toString()
        lifecycleScope.launch {
            LoginRepository().login(userName, passWord).collect { result ->
                result.fold(
                    onSuccess = { loginResponse ->
                        Log.d(TAG, "login: 收到账号密码分别是 ${userName} 和 ${passWord} 的登录请求，服务器返回状态码 ${loginResponse.code}")
                        if (loginResponse.code == 200) {
                            Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                            LoginStatusManager.saveLoginStatus(applicationContext, true, userName, identity)
                            val intent = if (identity.equals(IDENTITY_ELDER)) {
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
                        Log.d(TAG, "login: 收到账号密码分别是 ${userName} 和 ${passWord} 的登录请求但是失败")
                        Toast.makeText(this@LoginActivity, "请求失败：${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun fakelogin() {
        val userName = username.text.toString()
        LoginStatusManager.saveLoginStatus(applicationContext, true, userName, identity)
        val intent = if (identity.equals(IDENTITY_ELDER)) {
            Intent(this@LoginActivity, MainActivity::class.java)
        } else {
            Intent(this@LoginActivity, ChildrenActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
package com.example.cogwatch.splash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cognitive.main.ConMainActivity
import com.example.cogwatch.R
import com.example.cogwatch.login.ui.LoginActivity
import com.example.common.login.remote.LoginStatusManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // 仅Android 12+生效
            installSplashScreen() // 加载Theme.MyApp.Splash中的启动屏属性
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //debug_login()
        checkLoginStatusAndJump()

    }

    private fun checkLoginStatusAndJump() {
        val intent = if (LoginStatusManager.isLogin(this)) {
            // 已登录，跳主界面
            Intent(this, ConMainActivity::class.java)
        } else {
            // 未登录，跳登录界面
            Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finish() // 关闭启动页，避免返回时重新进入
    }

    private fun debug_login() {
            // 直接跳登录界面，绕过登录状态检查
            val intent = Intent(this, ConMainActivity::class.java)
            startActivity(intent)
            finish()
    }
}
package com.example.cogwatch

import com.example.cognitive.main.HomeFragment
import com.example.cognitive.main.MainViewModel


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.cognitive.R
import mine.ui.MineRecordFragment
import read_assessment.ui.RecordActivity
import schedule.ui.ScheduleActivity
import schulte.ui.SchulteGameActivity
import sports.data.StepForegroundService
import sports.vm.StepViewModel

private const val TAG = "MainActivity"
private const val REQ_NOTIFY = 1001
class MainActivity : AppCompatActivity() {

    private lateinit var btnHome: View
    private lateinit var btnMine: View
    private lateinit var homeFragment: Fragment
    private lateinit var mineFragment: Fragment
    private var currentFragment: Fragment? = null
    private val mainViewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnMine = findViewById<View>(R.id.mine_layout)
        btnHome = findViewById<View>(R.id.home_layout)

        if (needNotificationPermission()) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQ_NOTIFY
            )
        } else {
            startStepService()
        }
        mainViewModel.initTodaySaveYesterday()
        homeFragment = HomeFragment()
        switchFragment(homeFragment)
        initBottomClick()
    }

    private fun switchFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()


        if (currentFragment == null) {
            transaction.add(R.id.fragment_container, fragment).commit()
            currentFragment = fragment
            return
        } else {
            if (currentFragment === fragment) {
                return
            }
        }



        if (fragment.isAdded) {
            transaction.hide(currentFragment!!).show(fragment).commit()
        } else {
            transaction.hide(currentFragment!!).add(R.id.fragment_container, fragment).commit()
        }

        currentFragment = fragment
    }

    fun initBottomClick() {
        btnHome.setOnClickListener {
            switchFragment(homeFragment)
        }
        btnMine.setOnClickListener {
            if (!this::mineFragment.isInitialized) {
                mineFragment = MineRecordFragment()
            }
            switchFragment(mineFragment)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        if (requestCode == REQ_NOTIFY) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startStepService()
            } else {
                Toast.makeText(this, "通知权限未授予，可能无法正常接收步数提醒", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun needNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    }

    private fun startStepService() {
        val intent = Intent(this, StepForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 8.0+ 必须用这个启动前台服务
            startForegroundService(intent)
        } else {
            // 8.0以下用旧方法
            startService(intent)
        }
    }
}
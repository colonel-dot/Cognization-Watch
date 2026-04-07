package com.example.cognitive.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.cognitive.R
import geofence.vm.CognitiveGeofenceViewModel
import mine.ui.MineRecordFragment
import sports.data.StepForegroundService

private const val TAG = "MainActivity"
private const val REQ_NOTIFY = 1001
class ConMainActivity : AppCompatActivity() {

    private lateinit var btnHome: View
    private lateinit var btnMine: View
    private lateinit var homeFragment: Fragment
    private lateinit var mineFragment: Fragment
    private lateinit var btnCall: View
    private var currentFragment: Fragment? = null
    private val mainViewModel: MainViewModel by viewModels()
    private val geofenceViewModel: CognitiveGeofenceViewModel by viewModels()


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

        btnCall = findViewById<View>(R.id.call)
        btnCall.setOnClickListener {
            mainViewModel.debug_post()
        }

        if (needNotificationPermission()) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQ_NOTIFY
            )
        } else {
            startStepService()
        }
        homeFragment = HomeFragment()
        switchFragment(homeFragment)
        initBottomClick()

        // 初始化围栏监控
        (application as ConApplication).initGeofenceMonitoring(geofenceViewModel)
    }

    fun switchFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        android.util.Log.d(TAG, "switchFragment: " + fragment.javaClass.simpleName + ", addToBackStack: " + addToBackStack)
        val transaction = supportFragmentManager.beginTransaction()

        /*if (!addToBackStack) {
            supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }*/

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

        /*if (addToBackStack) {
            transaction.addToBackStack(null)
        }*/

        currentFragment = fragment
    }

    // 在 ConMainActivity 中
    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count > 0) {
            // 如果栈里有子页面（ReadFragment等），则正常回退
            supportFragmentManager.popBackStack()
            // 同步更新 currentFragment 指针（重要！）
            val topFragment = supportFragmentManager.fragments.lastOrNull { it.isVisible }
            currentFragment = topFragment
        } else if (currentFragment != homeFragment) {
            // 如果当前在 Mine，点击返回跳回 Home
            switchFragment(homeFragment)
        } else {
            super.onBackPressed() // 退出 App
        }
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
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
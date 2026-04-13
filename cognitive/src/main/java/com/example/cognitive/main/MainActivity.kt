package com.example.cognitive.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.alibaba.android.arouter.facade.template.IProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.example.cognitive.R
import com.example.cognitive.main.home.ui.HomeFragment
import com.example.common.bind_device.BindStatusManager
import com.example.common.login.GuestStateHolder
import com.example.common.login.LoginPopupProvider
import com.example.common.login.simulate.InsertData
import com.example.common.persistense.geofence.GeofenceRepository
import com.example.common.router.RouterPaths
import com.google.android.material.bottomnavigation.BottomNavigationView
import geofence.vm.GeofenceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mine.ui.RecordFragment
import setting.ui.SettingFragment
import sports.data.StepForegroundService

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private var currentFragment: Fragment? = null
    private lateinit var geofenceViewModel: GeofenceViewModel
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val multiplePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d(TAG, "权限请求结果: $permissions")
        var isActivityRecognitionGranted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val activityRecognitionResult = permissions[Manifest.permission.ACTIVITY_RECOGNITION]
            if (activityRecognitionResult == false) {
                isActivityRecognitionGranted = false
            }
        }
        if (isActivityRecognitionGranted) {
            Log.d(TAG, "必要权限已获得，准备启动服务")
            startStepService()
        } else {
            Toast.makeText(this, "需要活动识别权限才能计步", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bottomNavigation = findViewById(R.id.navigation)

        checkAndRequestPermissions()

        geofenceViewModel = ViewModelProvider(this)[GeofenceViewModel::class.java]

        initBottomNavigation()

        GeofenceRepository.initialize(this)

        if (intent?.getBooleanExtra("guest", false) == true) {
            GuestStateHolder.setGuest(true)
            InsertData.init(this)
            coroutineScope.launch(Dispatchers.IO) {
                InsertData.insertBehaviorData()
                InsertData.insertRiskData()
                InsertData.insertGeofenceData()
            }
        }

        val otherId = BindStatusManager.getBindStatus().second
        if (!otherId.isNullOrEmpty() && !GuestStateHolder.isGuest()) {
            geofenceViewModel.pullAndCreateGeofence(otherId)
        }

        ARouter.getInstance()
            .build(RouterPaths.POPUP_LOGIN)
            .navigation(this, object : NavigationCallback {
                override fun onFound(postcard: Postcard) {
                    Log.d(TAG, "路由找到: ${postcard.path}")
                    val provider = ARouter.getInstance().build(RouterPaths.POPUP_LOGIN).navigation() as IProvider
                    provider.init(this@MainActivity)
                    if (provider is LoginPopupProvider) {
                        provider.showPopup()
                    }
                }

                override fun onLost(postcard: Postcard) {
                    Log.e(TAG, "路由未找到: ${postcard.path}")
                    Toast.makeText(this@MainActivity, "路由未找到: ${postcard.path}", Toast.LENGTH_SHORT).show()
                }

                override fun onInterrupt(postcard: Postcard) {
                    Log.w(TAG, "路由被拦截: ${postcard.path}")
                }

                override fun onArrival(postcard: Postcard) {
                    Log.d(TAG, "路由到达: ${postcard.path}")
                }
            })
    }

    private fun initBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            val selectFragment: Fragment? = when (item.itemId) {
                R.id.home -> HomeFragment()
                R.id.history -> RecordFragment()
                R.id.settings -> SettingFragment()
                else -> null
            }
            selectFragment?.let { switchFragment(it) }
            true
        }
        switchFragment(HomeFragment())
    }

    fun switchFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val transaction = supportFragmentManager.beginTransaction()
        if (currentFragment == null) {
            transaction.add(R.id.fragment_container, fragment)
        } else {
            if (currentFragment == fragment) return
            transaction.hide(currentFragment!!)
            if (fragment.isAdded) {
                transaction.show(fragment)
            } else {
                transaction.add(R.id.fragment_container, fragment)
            }
        }
        transaction.commit()
        currentFragment = fragment
    }

    private fun checkAndRequestPermissions() {
        Log.d(TAG, "checkAndRequestPermissions")
        val permissionsToRequest = ArrayList<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            multiplePermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            startStepService()
        }
    }

    private fun startStepService() {
        Log.d(TAG, "startStepService")
        val intent = Intent(this, StepForegroundService::class.java)
        startForegroundService(intent)
    }

    fun isGuestLogin(): Boolean = GuestStateHolder.isGuest()
}

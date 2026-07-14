package com.example.bridge.main

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.alibaba.android.arouter.facade.template.IProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.example.bridge.R
import com.example.bridge.dashboard.ui.DashboardFragment
import com.example.bridge.geofence.feature.GeofenceStatusManager
import com.example.bridge.geofence.ui.GeofenceFragment
import com.example.bridge.record.ui.RecordFragment
import com.example.bridge.setting.ui.SettingFragment
import com.example.common.login.GuestStateHolder
import com.example.common.login.LoginPopupProvider
import com.example.common.login.simulate.InsertData
import com.example.common.router.RouterPaths
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ChildrenActivity"

class ChildrenActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.bridge_activity_children)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bottomNavigation = findViewById(R.id.navigation)

        if (intent?.getBooleanExtra("guest", false) == true) {
            initGuestDataThenNavigation()
        } else {
            initBottomNavigation()
        }

        ARouter.getInstance()
            .build(RouterPaths.POPUP_LOGIN)
            .greenChannel()
            .navigation(this, object : NavigationCallback {
                override fun onFound(postcard: Postcard) {
                    Log.d(TAG, "路由找到: ${postcard.path}")
                    val provider = ARouter.getInstance().build(RouterPaths.POPUP_LOGIN).navigation() as IProvider
                    provider.init(this@ChildrenActivity)
                    if (provider is LoginPopupProvider) {
                        provider.showPopup()
                    }
                }

                override fun onLost(postcard: Postcard) {
                    Log.e(TAG, "路由未找到: ${postcard.path}")
                    Toast.makeText(this@ChildrenActivity, "路由未找到: ${postcard.path}", Toast.LENGTH_SHORT).show()
                }

                override fun onInterrupt(postcard: Postcard) {
                    Log.w(TAG, "路由被拦截: ${postcard.path}")
                }

                override fun onArrival(postcard: Postcard) {
                    Log.d(TAG, "路由到达: ${postcard.path}")
                }
            })
    }

    private fun initGuestDataThenNavigation() {
        GuestStateHolder.setGuest(true)
        try {
            InsertData.init(this)
            GeofenceStatusManager.setFenceEnabled(this, true)
            GeofenceStatusManager.saveFenceInfo(this, "", 3000f, InsertData.HOME_LAT, InsertData.HOME_LNG)
        } catch (e: Exception) {
            Log.e(TAG, "初始化访客数据失败: ${e.message}", e)
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    InsertData.insertBehaviorData()
                    InsertData.insertRiskData()
                    InsertData.insertGeofenceData()
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e(TAG, "插入访客数据失败: ${e.message}", e)
                }
            }
            initBottomNavigation()
        }
    }

    private fun initBottomNavigation() {
        Log.d(TAG, "initBottomNavigation")
        bottomNavigation.setOnItemSelectedListener { item ->
            switchFragment(item.itemId)
            true
        }
        bottomNavigation.selectedItemId = R.id.dashboard
    }

    fun switchToGeofenceFragment() {
        bottomNavigation.selectedItemId = R.id.geofence
    }

    private fun switchFragment(itemId: Int) {
        Log.d(TAG, "Bottom nav item selected: $itemId")
        val targetTag: String?
        val selectedFragment: Fragment?
        when (itemId) {
            R.id.dashboard -> {
                targetTag = "DASHBOARD"
                selectedFragment = DashboardFragment()
                Log.d(TAG, "Dashboard selected")
            }
            R.id.data -> {
                targetTag = "DATA"
                selectedFragment = RecordFragment()
                Log.d(TAG, "Data selected")
            }
            R.id.geofence -> {
                targetTag = "GEOFENCE"
                selectedFragment = GeofenceFragment()
                Log.d(TAG, "Geofence selected")
            }
            R.id.settings -> {
                targetTag = "SETTINGS"
                selectedFragment = SettingFragment()
                Log.d(TAG, "Settings selected")
            }
            else -> {
                targetTag = null
                selectedFragment = null
            }
        }
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        Log.d(TAG, "Current fragment: $currentFragment, tag: ${currentFragment?.tag}")
        if (currentFragment?.tag == targetTag) {
            Log.d(TAG, "Already on target fragment, skipping")
            return
        }
        if (selectedFragment != null) {
            Log.d(TAG, "Committing fragment transaction for tag: $targetTag")
            try {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment, targetTag)
                    .commit()
                Log.d(TAG, "Fragment transaction committed")
            } catch (e: Exception) {
                Log.e(TAG, "Error committing fragment transaction", e)
            }
        } else {
            Log.e(TAG, "selectedFragment is null!")
        }
    }

    fun isGuestLogin(): Boolean = GuestStateHolder.isGuest()
}

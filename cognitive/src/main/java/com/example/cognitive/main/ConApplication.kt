package com.example.cognitive.main

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.common.bind_device.BindStatusManager
import com.example.common.persistense.geofence.GeofenceRepository
import geofence.vm.CognitiveGeofenceViewModel
import user.UserManager

class ConApplication: Application() {

    companion object {
        lateinit var context: Context
        private const val TAG = "ConApplication"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        BindStatusManager.init(this)
        UserManager.init(this)
        GeofenceRepository.initialize(this)
    }

    /**
     * 初始化老人设备的围栏监控
     * 由 ConMainActivity 在 UI 准备好后调用
     */
    fun initGeofenceMonitoring(viewModel: CognitiveGeofenceViewModel) {
        val otherId = UserManager.getOtherId()
        if (otherId.isNullOrEmpty()) {
            Log.w(TAG, "No bound child device, skip geofence init")
            return
        }
        Log.d(TAG, "Init geofence monitoring for child: $otherId")
        viewModel.pullAndCreateGeofence(otherId)
    }
}
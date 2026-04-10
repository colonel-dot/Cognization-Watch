package sports.data

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.cognitive.R
import com.example.common.persistense.AppDatabase

private const val TAG = "StepForegroundService"

class StepForegroundService : Service() {

    private var repo: StepRepository? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "服务已经打开")
        val sensorManager = getSystemService(SENSOR_SERVICE) as? SensorManager
        val db = AppDatabase.getDatabase(application)

        if (sensorManager != null) {
            repo = StepRepository(
                sensorManager = sensorManager,
                dao = db.dailyBehaviorDao()
            )
            repo?.start()
        } else {
            Log.e(TAG, "无法获取 SensorManager，步数记录功能将不可用")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        val nm = getSystemService(NotificationManager::class.java)
        val notificationsEnabled = nm.areNotificationsEnabled()
        Log.d(TAG, "系统通知全局开关: $notificationsEnabled")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotifyPermission = checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "Android 13+ 通知权限授予状态: $hasNotifyPermission")
        }

        // 检查通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = nm.getNotificationChannel(CHANNEL_ID)
            if (channel != null) {
                Log.d(TAG, "通知渠道详情 - 名称: ${channel.name}, 重要性: ${channel.importance}, ID: ${channel.id}")
            } else {
                Log.d(TAG, "通知渠道未找到，将创建")
            }
        }

    }

    override fun onDestroy() {
        repo?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int = START_STICKY


    private fun createNotification(): Notification {
        createChannel()
        Log.d(TAG, "正在创建通知，以保持前台服务运行")
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("运动记录中")
            .setContentText("正在统计今日步数和活动时间")
            .setSmallIcon(R.drawable.ic_walk)
            .setOngoing(true)

        // 为 Android 7.1 及以下版本设置优先级
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.priority = NotificationCompat.PRIORITY_DEFAULT
        }

        return builder.build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // 检查渠道是否已存在
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel != null) {
                Log.d(TAG, "通知渠道已存在: ${existingChannel.name}, 重要性: ${existingChannel.importance}")
                return
            }

            val channel = NotificationChannel(
                CHANNEL_ID,
                "运动统计",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "用于显示步数记录服务的运行状态"
                // 在 Android 8.0-11 上，锁屏通知默认隐藏，可以设置是否显示
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setShowBadge(false) // 不在应用图标上显示角标
                }
                // 设置锁屏可见性
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "创建通知渠道: 运动统计, 重要性: LOW")
        }
    }

    companion object {
        private const val CHANNEL_ID = "step_channel"
        private const val NOTIFICATION_ID = 1001
    }
}

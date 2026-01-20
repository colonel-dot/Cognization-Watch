package sports.data

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.cognitive.R
import persistense.AppDatabase

private const val TAG = "StepForegroundService"

class StepForegroundService : Service() {

    private lateinit var repo: StepRepository

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "服务已经打开了")
        val sensorManager =
            getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val db = AppDatabase.getDatabase(application)


        repo = StepRepository(
            sensorManager = sensorManager,
            dao = db.dailyBehaviorDao()
        )

        startForeground(NOTIFICATION_ID, createNotification())
        repo.start()

        val nm = getSystemService(NotificationManager::class.java)
        Log.d(TAG, "notifications enabled = ${nm.areNotificationsEnabled()}")

    }

    override fun onDestroy() {
        repo.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int = START_STICKY

    // -------------------- Notification --------------------

    private fun createNotification(): Notification {
        createChannel()
        Log.d(TAG, "正在创建通知，以保持前台服务运行")
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("运动记录中")
            .setContentText("正在统计今日步数和活动时间")
            .setSmallIcon(R.drawable.ic_walk)
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "运动统计",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "step_channel"
        private const val NOTIFICATION_ID = 1001
    }
}

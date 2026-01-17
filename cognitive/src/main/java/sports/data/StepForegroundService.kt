package sports.data

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import sports.data.StepRepository
import com.example.cognitive.R

class StepForegroundService : Service() {

    private lateinit var repo: StepRepository

    override fun onCreate() {
        super.onCreate()
        repo = StepRepository(applicationContext)
        startForeground(1, buildNotification())
        repo.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        repo.stop()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "step_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "运动统计",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("运动统计中")
            .setContentText("正在记录步数和运动时间")
            .setSmallIcon(R.drawable.ic_walk)
            .setOngoing(true)
            .build()
    }
}

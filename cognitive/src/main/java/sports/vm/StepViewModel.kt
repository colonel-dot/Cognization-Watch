package sports.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.os.Handler
import android.os.Looper

class StepViewModel(app: Application) : AndroidViewModel(app) {

    val stepCount = MutableLiveData(0)
    val activeTime = MutableLiveData(0)
    val restTime = MutableLiveData(0)

    private val sp =
        app.getSharedPreferences("step", Application.MODE_PRIVATE)

    private val handler = Handler(Looper.getMainLooper())

    init {
        handler.post(object : Runnable {
            override fun run() {
                activeTime.value = sp.getInt("todayActiveTime", 0)
                restTime.value = sp.getInt("todayRestTime", 0)
                handler.postDelayed(this, 1000)
            }
        })
    }
}

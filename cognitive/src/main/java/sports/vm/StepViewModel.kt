package sports.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import sports.data.StepRepository

class StepViewModel(application: Application) : AndroidViewModel(application) {

    val stepCount = MutableLiveData(0)

    // 新增两个时间 LiveData（单位：秒）
    val activeTime = MutableLiveData(0)  // 今日运动时间
    val restTime = MutableLiveData(0)    // 今日休息时间

    private val repo = StepRepository(application)

    init {
        // 步数回调
        repo.onStepChanged = { steps ->
            stepCount.postValue(steps)
        }

        // 时间回调（运动 + 休息）
        repo.onTimeChanged = { active, rest ->
            activeTime.postValue(active)
            restTime.postValue(rest)
        }
    }

    fun startCounting() = repo.start()
    fun stopCounting() = repo.stop()
}

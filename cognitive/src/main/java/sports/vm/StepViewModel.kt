package sports.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.common.persistense.AppDatabase
import kotlinx.coroutines.launch
import java.time.LocalDate


class StepViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).dailyBehaviorDao()

    private val _stepCountLive = MutableLiveData<Double>()
    val stepCount: LiveData<Double> = _stepCountLive

    init {
        // 初始加载
        loadSteps()
    }

    fun loadSteps() {
        viewModelScope.launch {
            try {
                val entity = dao.getOrInitTodayBehavior(LocalDate.now())
                val steps = entity.steps?.toDouble() ?: 0.0
                _stepCountLive.postValue(steps)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refresh() {
        loadSteps()
    }
}

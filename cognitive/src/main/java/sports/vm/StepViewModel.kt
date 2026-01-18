package sports.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import persistense.DailyBehaviorDatabase
import java.time.LocalDate

class StepViewModel(application: Application) : AndroidViewModel(application) {

    val stepCount = MutableLiveData(0)

    private val dao =
        DailyBehaviorDatabase.getDatabase(application).dailyBehaviorDao()

    fun refreshToday() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val entity = dao.getOrInitTodayBehavior(today)
            stepCount.postValue(entity.steps)
        }
    }
}

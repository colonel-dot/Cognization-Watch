package sports.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import persistense.AppDatabase
import java.time.LocalDate


class StepViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).dailyBehaviorDao()


    val stepCount: LiveData<Double> = dao.observeBehaviorByDate(LocalDate.now())
        .map { entity ->
            entity.steps?.toDouble() ?: 0.0
        }
        .asLiveData(viewModelScope.coroutineContext)
}

package schulte.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import persistense.AppDatabase
import repository.UpdateRepository
import risk.work.RiskConfigManager
import schulte.data.SchulteEvaluatorType
import java.time.LocalDate

private const val TAG = "SchulteGameViewModel"

class SchulteGameViewModel(application: Application) : AndroidViewModel(application) {

    private val behaviorDatabase = AppDatabase.getDatabase(application)
    private val behaviorDao = behaviorDatabase.dailyBehaviorDao()

    fun saveGameTime(gridSize: Int, elapsedTime: Long) {
        Log.d(TAG, "舒尔特完成 准备更新成绩")

        viewModelScope.launch {
            val today = LocalDate.now()
            behaviorDao.getOrInitTodayBehavior(today)

            when (gridSize) {

                4 -> {
                    val lastTime = behaviorDao.getByDate(today)!!.schulte16TimeSec!!

                    if (lastTime < 0.1 || elapsedTime.toDouble() < lastTime) {
                        UpdateRepository.update16Schulte(
                            time = elapsedTime.toDouble()
                        )
                    }
                }

                5 -> {
                    val lastTime = behaviorDao.getByDate(today)!!.schulte25TimeSec!!

                    if (lastTime < 0.1 || elapsedTime.toDouble() < lastTime) {
                        UpdateRepository.update25Schulte(
                            time = elapsedTime.toDouble()
                        )
                    }
                }
            }
        }
    }

    fun saveEvaluationType(type: SchulteEvaluatorType) {
        RiskConfigManager(getApplication())
            .setSchulteEvaluatorType(type)
    }
}
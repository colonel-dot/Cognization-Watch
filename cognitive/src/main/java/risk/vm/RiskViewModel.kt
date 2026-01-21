package risk.vm

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import persistense.AppDatabase
import risk.persistence.DailyRiskEntity
import java.time.LocalDate

class RiskViewModel(application: Application) : AndroidViewModel(application) {

    private val riskDao = AppDatabase
        .getDatabase(application)
        .dailyRiskDao()

    private val _riskData = MutableLiveData<DailyRiskEntity?>()
    val riskData: LiveData<DailyRiskEntity?> = _riskData

    fun queryRiskByDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                val risk = riskDao.getByDate(date)   //  可能为 null，天然安全
                _riskData.postValue(risk)
            } catch (e: Exception) {
                _riskData.postValue(null)  // 任何异常也当作“无风险记录”
            }
        }
    }
}

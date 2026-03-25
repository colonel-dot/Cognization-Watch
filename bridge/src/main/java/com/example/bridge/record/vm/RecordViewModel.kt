package com.example.cogwatch_ui.children.record.vm

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import com.example.common.persistense.AppDatabase
import com.example.common.persistense.behavior.DailyBehaviorEntity
import com.example.common.persistense.risk.DailyRiskEntity
import java.time.LocalDate

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val riskDao = AppDatabase
        .getDatabase(application)
        .dailyRiskDao()

    private val behaviorDao = AppDatabase
        .getDatabase(application)
        .dailyBehaviorDao()

    private val _riskData = MutableLiveData<DailyRiskEntity?>()
    val riskData: LiveData<DailyRiskEntity?> = _riskData

    private val _behaviorData = MutableLiveData<DailyBehaviorEntity?>()
    val behaviorData: LiveData<DailyBehaviorEntity?> = _behaviorData

    fun queryRiskByDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                val risk = riskDao.getByDate(date)   //  可能为 null，天然安全
                _riskData.postValue(risk)
            } catch (e: Exception) {
                _riskData.postValue(null)  // 任何异常也当作“无风险记录”
            }
            try {
                val behavior = behaviorDao.getByDate(date)
                _behaviorData.postValue(behavior)
            } catch (e: Exception) {
                _behaviorData.postValue(null)
            }
        }
    }
}

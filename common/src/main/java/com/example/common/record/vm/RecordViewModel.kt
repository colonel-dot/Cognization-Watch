package com.example.common.record.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.common.persistense.AppDatabase
import com.example.common.persistense.behavior.DailyBehaviorEntity
import com.example.common.persistense.risk.DailyRiskEntity
import kotlinx.coroutines.launch
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
                val risk = riskDao.getByDate(date)
                _riskData.postValue(risk)
            } catch (_: Exception) {
                _riskData.postValue(null)
            }
            try {
                val behavior = behaviorDao.getByDate(date)
                _behaviorData.postValue(behavior)
            } catch (_: Exception) {
                _behaviorData.postValue(null)
            }
        }
    }
}
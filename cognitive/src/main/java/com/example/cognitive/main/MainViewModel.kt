package com.example.cognitive.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import persistense.AppDatabase
import risk.work.DailyRiskCalculator
import risk.model.toEntity
import risk.model.toNormalizedList
import risk.work.RiskConfigManager
import java.time.LocalDate

class MainViewModel(application: Application): AndroidViewModel(application) {

    val today = LocalDate.now()
    private val appDatabase = AppDatabase.getDatabase(application)
    private val behaviorDao = appDatabase.dailyBehaviorDao()
    private val riskDao = appDatabase.dailyRiskDao()
    private val riskConfigManager = RiskConfigManager(getApplication())

    private val _recordChanged = MutableLiveData<Unit>()
    val recordChanged: LiveData<Unit> = _recordChanged

    fun initTodaySaveYesterday() {
        viewModelScope.launch {
            behaviorDao.getOrInitTodayBehavior(today)
            delay(10)
            val behaviorRecords = behaviorDao.loadPrev15Days(today)
            val evaluatorType = riskConfigManager.getSchulteEvaluatorType()
            val riskResult = DailyRiskCalculator
                .calculate(behaviorRecords.toNormalizedList(), evaluatorType)

            riskDao.upsert(riskResult.toEntity())
        }
    }

    fun notifyRecordChanged() {
        _recordChanged.postValue(Unit)
    }

}

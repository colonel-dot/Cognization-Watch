package com.example.cognitive.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import debug_simulate.InsertData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import persistense.AppDatabase
import repository.NetWorkRepository
import risk.work.DailyRiskCalculator
import risk.model.toEntity
import risk.model.toNormalizedList
import risk.work.RiskConfigManager
import user.UserManager
import java.time.LocalDate

private const val TAG = "MainViewModel"

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
           // Log.d(TAG, "initTodaySaveYesterday: today = $today")
            //以下为调试代码
            InsertData.init(getApplication())
            InsertData.insertBehaviorData()
            InsertData.insertRiskData()
            //以上为调试代码
            val todayBehavior = behaviorDao.getOrInitTodayBehavior(today)
            val behaviorRecords = behaviorDao.loadPrev15Days(today)
            val evaluatorType = riskConfigManager.getSchulteEvaluatorType()
            val riskResult = DailyRiskCalculator
                .calculate(behaviorRecords.toNormalizedList(), evaluatorType)
            riskDao.upsert(riskResult.toEntity())

            NetWorkRepository.updateDailyBehavior(account = UserManager.getUserId(), date = today, todayBehavior)
            NetWorkRepository.updateDailyRisk(account = UserManager.getUserId(), date = today.minusDays(1), riskResult)
        }
    }

    fun notifyRecordChanged() {
        _recordChanged.postValue(Unit)
    }

}

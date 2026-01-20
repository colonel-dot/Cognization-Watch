package com.example.cognitive.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import persistense.AppDatabase
import persistense.DailyBehaviorEntity
import risk.model.NormalizedDailyBehavior
import risk.DailyRiskCalculator
import risk.model.DailyRiskResult
import risk.model.toEntity
import risk.model.toNormalizedList
import java.time.LocalDate

class MainViewModel(application: Application): AndroidViewModel(application) {

    val today = LocalDate.now()
    private val appDatabase = AppDatabase.getDatabase(application)
    private val behaviorDao = appDatabase.dailyBehaviorDao()
    private val riskDao = appDatabase.dailyRiskDao()

    fun initTodayBehavior() {
        viewModelScope.launch {
            behaviorDao.getOrInitTodayBehavior(today)
        }
    }

    fun saveYesterdayRiskResult() {
        viewModelScope.launch {
            val behaviorRecords = behaviorDao.loadPrev15Days(today)
            val riskResult = DailyRiskCalculator
                .calculate(behaviorRecords.toNormalizedList())

            riskDao.upsert(riskResult.toEntity())
        }
    }
}

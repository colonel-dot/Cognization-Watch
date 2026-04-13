package com.example.cognitive.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.common.login.GuestStateHolder
import com.example.common.login.simulate.InsertData
import kotlinx.coroutines.launch
import com.example.common.persistense.AppDatabase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import repository.NetWorkRepository
import repository.UpdateRepository
import risk.work.DailyRiskCalculator
import risk.model.toEntity
import risk.model.toNormalizedList
import risk.model.toResult
import risk.work.RiskConfigManager
import user.UserManager
import java.time.LocalDate

private const val TAG = "MainViewModel"

class MainViewModel(application: Application): AndroidViewModel(application) {

    val today = LocalDate.now()!!
    private val appDatabase = AppDatabase.getDatabase(application)
    private val behaviorDao = appDatabase.dailyBehaviorDao()
    private val riskDao = appDatabase.dailyRiskDao()
    private val riskConfigManager = RiskConfigManager(getApplication())

    private val _recordChanged = MutableLiveData<Unit>()
    val recordChanged: LiveData<Unit> = _recordChanged

    fun initTodaySaveYesterday() {
        viewModelScope.launch {

            // TODO
            // Log.d(TAG, "initTodaySaveYesterday: today = $today")
            // 以下为调试代码
            // InsertData.init(getApplication())
            // InsertData.insertBehaviorData()
            // InsertData.insertRiskData()
            // 以上为调试代码

            val todayBehavior = behaviorDao.getOrInitTodayBehavior(today)
            val behaviorRecords = behaviorDao.loadPrev15Days(today)
            val evaluatorType = riskConfigManager.getSchulteEvaluatorType()
            val riskResult = DailyRiskCalculator
                .calculate(behaviorRecords.toNormalizedList(), evaluatorType)
            if (!GuestStateHolder.isGuest()) {
                riskDao.upsert(riskResult.toEntity())
            }

            // 以下为调试代码
            // debug_post()
            // 以上为调试代码

            Log.d(TAG, "initTodaySaveYesterday: 要给更新仓库传过去的todayBehavior是 $todayBehavior")
            UpdateRepository.initToday(todayBehavior)
            NetWorkRepository.updateDailyBehavior(account = UserManager.getUserId(), date = today, todayBehavior)
                .catch { e -> Log.e(TAG, "上传今日行为数据失败: ${e.message}") }
                .launchIn(viewModelScope)
            NetWorkRepository.updateDailyRisk(account = UserManager.getUserId(), date = today.minusDays(1), riskResult)
                .catch { e -> Log.e(TAG, "上传昨日风险数据失败: ${e.message}") }
                .launchIn(viewModelScope)
        }
    }

    fun notifyRecordChanged() {
        _recordChanged.postValue(Unit)
    }
}

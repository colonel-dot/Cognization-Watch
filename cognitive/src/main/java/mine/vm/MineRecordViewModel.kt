package mine.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import persistense.DailyBehaviorEntity
import java.time.LocalDate

private const val TAG = "MineRecordViewModel"

class MineRecordViewModel(application: Application): AndroidViewModel(application) {
    private val _todayBehaviorData = MutableLiveData<DailyBehaviorEntity?>()
    val todayBehaviorData: LiveData<DailyBehaviorEntity?> = _todayBehaviorData
    private val behaviorDao = persistense.DailyBehaviorDatabase
        .getDatabase(application)
        .dailyBehaviorDao()

    // 核心：查询当日所有数据的方法，外部调用这个方法即可
    fun queryTodayBehaviorData() {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val todayData = behaviorDao.getByDate(today)
                _todayBehaviorData.postValue(todayData) // 给LiveData赋值，通知UI更新
            } catch (e: Exception) {
                Log.e(TAG, "查询当日数据失败:${e.message}", e)
                _todayBehaviorData.postValue(null)
            }
        }
    }
}
package com.example.cognitive.mine.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.cognitive.mine.data.RecordModel
import com.example.common.persistense.risk.DailyRiskEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "MineRecordViewModel"

class RecordViewModel(application: Application) : AndroidViewModel(application) {
    private val recordModel = RecordModel(application)

    private val _todayRiskData = MutableStateFlow<DailyRiskEntity?>(null)
    val todayRiskData: StateFlow<DailyRiskEntity?> = _todayRiskData.asStateFlow()

    private val _allRiskData = MutableStateFlow<List<DailyRiskEntity>>(emptyList())
    val allRiskData: StateFlow<List<DailyRiskEntity>> = _allRiskData.asStateFlow()

    /** Java 互操作：为 RecordFragment 提供 LiveData 桥接 */
    fun getAllRiskDataLiveData(): LiveData<List<DailyRiskEntity>> = allRiskData.asLiveData()

    fun queryTodayRecordData() {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val todayData = recordModel.queryRiskByDate(today)
                _todayRiskData.value = todayData
            } catch (e: Exception) {
                Log.e(TAG, "查询当日数据失败: ${e.message}", e)
                _todayRiskData.value = null
            }
        }
    }

    fun queryRecordsData() {
        viewModelScope.launch {
            try {
                val recordsList: List<DailyRiskEntity> = recordModel.queryAllRiskRecords()
                _allRiskData.value = recordsList.reversed()
            } catch (e: Exception) {
                Log.e(TAG, "查询历史数据失败: ${e.message}", e)
            }
        }
    }

    fun queryRecordsByDays(days: Int) {
        viewModelScope.launch {
            try {
                val fromDate = LocalDate.now().minusDays((days - 1).toLong())
                val recordsList = recordModel.queryRiskRecords(fromDate)
                _allRiskData.value = recordsList.reversed()
            } catch (e: Exception) {
                Log.e(TAG, "查询近${days}天数据失败: ${e.message}", e)
            }
        }
    }
}

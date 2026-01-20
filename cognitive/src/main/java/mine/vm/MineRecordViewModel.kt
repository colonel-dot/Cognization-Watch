package mine.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mine.data.MineRecordModel
import persistense.DailyBehaviorEntity
import java.time.LocalDate

private const val TAG = "MineRecordViewModel"

class MineRecordViewModel(application: Application): AndroidViewModel(application) {
    private val _todayBehaviorData = MutableLiveData<DailyBehaviorEntity?>()
    private val _allBehaviorData = MutableLiveData<List<DailyBehaviorEntity>>()

    val todayBehaviorData: LiveData<DailyBehaviorEntity?> = _todayBehaviorData
    val allBehaviorData: LiveData<List<DailyBehaviorEntity>> = _allBehaviorData


    private val recordModel = MineRecordModel(application)

    fun queryRecordData() {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val todayData = recordModel.queryRecordDataByDate(today) // 调用仓库方法
                _todayBehaviorData.postValue(todayData)
            } catch (e: Exception) {
                Log.e(TAG, "查询当日数据失败:${e.message}", e)
                _todayBehaviorData.postValue(null)
            }
        }
    }

    fun queryRecordsData() {
        viewModelScope.launch {
            try {
                val recordsList: List<DailyBehaviorEntity> = recordModel.queryAllRecords()
                _allBehaviorData.postValue(recordsList.reversed())
            } catch (e: Exception) {
                Log.e(TAG, "查询历史数据失败:${e.message}", e)
            }
        }
    }
}
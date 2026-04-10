package mine.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import mine.data.RecordModel
import com.example.common.persistense.risk.DailyRiskEntity
import java.time.LocalDate
import kotlinx.coroutines.launch

private const val TAG = "MineRecordViewModel"

class RecordViewModel(application: Application) : AndroidViewModel(application) {
    private val recordModel = RecordModel(application)

    private val _todayRiskData = MutableLiveData<DailyRiskEntity?>()
    val todayRiskData: LiveData<DailyRiskEntity?> = _todayRiskData

    private val _allRiskData = MutableLiveData<List<DailyRiskEntity>>()
    val allRiskData: LiveData<List<DailyRiskEntity>> = _allRiskData

    fun queryTodayRecordData() {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val todayData = recordModel.queryRiskByDate(today)
                _todayRiskData.postValue(todayData)
            } catch (e: Exception) {
                Log.e(TAG, "查询当日数据失败: ${e.message}", e)
                _todayRiskData.postValue(null)
            }
        }
    }

    fun queryRecordsData() {
        viewModelScope.launch {
            try {
                val recordsList: List<DailyRiskEntity> = recordModel.queryAllRiskRecords()
                _allRiskData.postValue(recordsList.reversed())
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
                _allRiskData.postValue(recordsList.reversed())
            } catch (e: Exception) {
                Log.e(TAG, "查询近${days}天数据失败: ${e.message}", e)
            }
        }
    }
}

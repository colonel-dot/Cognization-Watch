package schulte.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import schulte.data.SchulteCell
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import persistense.AppDatabase
import java.time.LocalDate

enum class GameState {
    READY, RUNNING, FINISHED
}

class SchulteGameViewModel(application: Application) : AndroidViewModel(application) {

    //companion object {
        private var GRID_SIZE = 5
    //}

    private val _cells = MutableLiveData<List<SchulteCell>>()
    val cells: LiveData<List<SchulteCell>> = _cells

    private val _nextNumber = MutableLiveData<Int>()
    val nextNumber: LiveData<Int> = _nextNumber

    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long> = _elapsedTime

    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    private var startTime: Long = 0L
    private var timerJob: Job? = null
    private var maxNumber = GRID_SIZE * GRID_SIZE

    val behaviorDatabase = AppDatabase.getDatabase(application)
    val behaviorDao = behaviorDatabase.dailyBehaviorDao()

    init {
        resetGame()
    }

    fun resetGame() {
        val nums = (1..maxNumber).shuffled()
        _cells.value = nums.map { SchulteCell(number = it) }
        _nextNumber.value = 1
        _elapsedTime.value = 0L
        _gameState.value = GameState.READY
    }

    fun changeGridSize(size: Int) {
        GRID_SIZE = size
        maxNumber = GRID_SIZE * GRID_SIZE
        resetGame()
    }

    fun startGame() {
        if (_gameState.value == GameState.RUNNING) return
        _gameState.value = GameState.RUNNING
        startTime = System.currentTimeMillis()
        startTimer()
    }

    fun onCellClicked(number: Int) {
        if (_gameState.value == GameState.READY) {
            // 第一次点击 1 时开始计时
            if (number == 1) {
                startGame()
            } else {
                return
            }
        }

        if (_gameState.value != GameState.RUNNING) return

        val expected = _nextNumber.value ?: 1
        if (number != expected) {
            // 暂时不处理错误逻辑，最小 demo 先能跑
            return
        }

        if (expected == maxNumber) {
            // 最后一个点完
            _nextNumber.value = expected
            finishGame()
        } else {
            _nextNumber.value = expected + 1
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _gameState.value == GameState.RUNNING) {
                val now = System.currentTimeMillis()
                _elapsedTime.postValue(now - startTime)
                delay(50)
            }
        }
    }

    private fun finishGame() {
        _gameState.value = GameState.FINISHED
        timerJob?.cancel()
        // 最后一帧时间更新
        val now = System.currentTimeMillis()
        val finishedTime = now - startTime
        saveGameTime(finishedTime)
        _elapsedTime.value = finishedTime
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    fun saveGameTime(elapsedTime: Long) {
        viewModelScope.launch {
            val today = LocalDate.now()
            behaviorDao.getOrInitTodayBehavior(today)
            if (GRID_SIZE == 4) {
                val lastTime = behaviorDao.getByDate(today)!!.schulte16TimeSec!!
                if (lastTime < 0.1 || elapsedTime.toDouble() < lastTime) {
                    behaviorDao.updateSchulte16Time(today, elapsedTime.toDouble())
                }
            } else if (GRID_SIZE == 5) {
                val lastTime = behaviorDao.getByDate(today)!!.schulte25TimeSec!!
                if (lastTime < 0.1 || elapsedTime.toDouble() < lastTime) {
                    behaviorDao.updateSchulte25Time(today, elapsedTime.toDouble())
                }
            }

        }
    }
}

package com.example.cognitive.schulte.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cognitive.schulte.data.SchulteCell
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class GameState {
    READY, RUNNING, FINISHED
}

class SchulteGameViewModel : ViewModel() {

    companion object {
        private const val GRID_SIZE = 5
    }

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
    private val maxNumber = GRID_SIZE * GRID_SIZE

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
        _elapsedTime.value = now - startTime
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

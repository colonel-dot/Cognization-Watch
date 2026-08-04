package com.example.cognitive.sports.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.persistense.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate


class StepViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).dailyBehaviorDao()
    private val today = LocalDate.now()

    init {
        // 确保当日行存在，DAO Flow 将自动推送数据
        viewModelScope.launch {
            dao.getOrInitTodayBehavior(today)
        }
    }

    val stepCount: StateFlow<Double> = dao.observeBehaviorByDate(today)
        .map { entity -> entity?.steps?.toDouble() ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)
}

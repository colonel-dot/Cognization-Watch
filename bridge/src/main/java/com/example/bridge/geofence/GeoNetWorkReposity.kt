package com.example.bridge.geofence

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GeoNetWorkReposity {
    fun getBarrierInfo(): Flow<Result<BarrierInfo>> = flow {
        TODO()
    }

    fun postBarrierInfo(barrierInfo: BarrierInfo): Flow<Result<Int>> = flow {
        TODO()

    }

    fun getElderMovement(): Flow<Result<ElderMovement>> = flow {
        TODO()
    }

    fun postElderMovement(elderMovement: ElderMovement): Flow<Result<Int>> {
        TODO()
    }
}
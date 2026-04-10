package com.example.common.persistense.geofence

import kotlinx.coroutines.runBlocking

object GeofencePersistenceProxy {
    // TODO
    @JvmStatic
    fun getByStatusSync(dao: GeofenceItemDao, status: Int): List<GeofenceItem> {
        return runBlocking { dao.getByStatus(status) }
    }
}
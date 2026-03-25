package com.example.common.persistense.geofence

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.jvm.JvmStatic

object GeofenceRepository {

    private lateinit var dao: GeofenceItemDao

    @JvmStatic
    fun initialize(context: Context) {
        if (!::dao.isInitialized) {
            val database = GeofenceDatabase.getDatabase(context)
            dao = database.geofenceItemDao()
        }
    }

    private fun ensureInitialized() {
        if (!::dao.isInitialized) {
            throw IllegalStateException("GeofenceRepository not initialized. Call initialize(context) first.")
        }
    }

    // 插入事件（后台协程）
    fun insertEvent(item: GeofenceItem) {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            dao.insert(item)
        }
    }

    // 阻塞方式插入（用于 BroadcastReceiver）
    @JvmStatic
    fun insertEventBlocking(item: GeofenceItem) {
        ensureInitialized()
        runBlocking(Dispatchers.IO) {
            dao.insert(item)
        }
    }

    // 获取所有事件（阻塞）
    fun getAllEventsBlocking(): List<GeofenceItem> {
        ensureInitialized()
        return runBlocking(Dispatchers.IO) {
            dao.getAll()
        }
    }

    // 获取所有事件（Flow）
    fun getAllEventsFlow() = dao.getAllFlow()

    // 按状态查询（阻塞）
    fun getEventsByStatusBlocking(status: Int): List<GeofenceItem> {
        ensureInitialized()
        return runBlocking(Dispatchers.IO) {
            dao.getByStatus(status)
        }
    }

    // 按时间范围查询（阻塞）
    fun getEventsInRangeBlocking(start: Int, end: Int): List<GeofenceItem> {
        ensureInitialized()
        return runBlocking(Dispatchers.IO) {
            dao.loadRange(start, end)
        }
    }

    // 按时间范围查询（Flow）
    fun getEventsInRangeFlow(start: Int, end: Int) = dao.loadRangeFlow(start, end)

    // 删除所有事件（后台协程）
    fun deleteAllEvents() {
        ensureInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            dao.deleteAll()
        }
    }

    // 删除特定事件（阻塞）
    fun deleteEventBlocking(item: GeofenceItem) {
        ensureInitialized()
        runBlocking(Dispatchers.IO) {
            dao.delete(item)
        }
    }
}
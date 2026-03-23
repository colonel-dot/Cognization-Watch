package persistense.geofence

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GeofenceItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<GeofenceItem>)

    @Update
    suspend fun update(item: GeofenceItem)

    @Delete
    suspend fun delete(item: GeofenceItem)

    @Query("DELETE FROM geofence_event")
    suspend fun deleteAll()

    @Query("SELECT * FROM geofence_event WHERE id = :id")
    suspend fun getById(id: Long): GeofenceItem?

    @Query("SELECT * FROM geofence_event ORDER BY timestamp DESC")
    suspend fun getAll(): List<GeofenceItem>

    @Query("SELECT * FROM geofence_event ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<GeofenceItem>>

    @Query("SELECT * FROM geofence_event WHERE status = :status ORDER BY timestamp DESC")
    suspend fun getByStatus(status: Int): List<GeofenceItem>

    @Query("SELECT * FROM geofence_event WHERE timestamp >= :from ORDER BY timestamp DESC")
    suspend fun loadFrom(from: Int): List<GeofenceItem>

    @Query("SELECT * FROM geofence_event WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    suspend fun loadRange(start: Int, end: Int): List<GeofenceItem>

    @Query("SELECT * FROM geofence_event WHERE timestamp >= :from ORDER BY timestamp DESC")
    fun loadFromFlow(from: Int): Flow<List<GeofenceItem>>

    @Query("SELECT * FROM geofence_event WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun loadRangeFlow(start: Int, end: Int): Flow<List<GeofenceItem>>
}
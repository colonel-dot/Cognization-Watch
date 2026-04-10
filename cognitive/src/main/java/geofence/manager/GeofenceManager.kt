package geofence.manager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.amap.api.fence.GeoFenceClient
import com.amap.api.fence.GeoFenceListener
import com.amap.api.location.DPoint
import com.example.common.geofence.GeofenceConstants
import com.example.common.geofence.model.BarrierInfo
import androidx.core.content.edit

class GeofenceManager(context: Context) {

    companion object {
        private const val TAG = "CognitiveGeofenceMgr"
        const val GEOFENCE_BROADCAST_ACTION = GeofenceConstants.GEOFENCE_BROADCAST_ACTION
        private const val SP_NAME = "cognitive_geofence_status"
        private const val KEY_ELDERNAME = "eldername"
        private const val KEY_LAT = "lat"
        private const val KEY_LON = "lon"
        private const val KEY_RADIUS = "radius"
    }

    private val context: Context = context.applicationContext
    private val geoFenceClient: GeoFenceClient = GeoFenceClient(context)
    private val sp: SharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)

    private var currentFenceId: String? = null

    init {
        geoFenceClient.setActivateAction(
            GeoFenceClient.GEOFENCE_IN or
            GeoFenceClient.GEOFENCE_OUT or
            GeoFenceClient.GEOFENCE_STAYED
        )
        geoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION)
    }

    fun setFenceListener(listener: GeoFenceListener) {
        geoFenceClient.setGeoFenceListener(listener)
    }

    fun createGeofence(barrierInfo: BarrierInfo): Boolean {
        val customId = barrierInfo.eldername
        if (customId == currentFenceId) {
            Log.w(TAG, "Geofence already created for: $customId")
            return false
        }

        val center = DPoint(barrierInfo.lat, barrierInfo.lon)
        val radius = barrierInfo.radius.toFloat()

        geoFenceClient.addGeoFence(center, radius, customId)
        currentFenceId = customId

        saveBarrierInfo(barrierInfo)

        Log.d(TAG, "Geofence created: eldername=$customId, lat=${barrierInfo.lat}, lon=${barrierInfo.lon}, radius=$radius")
        return true
    }

    fun restoreGeofenceIfExists(): Boolean {
        val eldername = sp.getString(KEY_ELDERNAME, null) ?: return false
        val lat = sp.getFloat(KEY_LAT, 0f).toDouble()
        val lon = sp.getFloat(KEY_LON, 0f).toDouble()
        val radius = sp.getFloat(KEY_RADIUS, 0f).toDouble()

        if (lat == 0.0 && lon == 0.0) {
            return false
        }

        val barrierInfo = BarrierInfo(eldername, lon, lat, radius)
        Log.d(TAG, "Restoring geofence from local: $barrierInfo")
        return createGeofence(barrierInfo)
    }

    private fun saveBarrierInfo(barrierInfo: BarrierInfo) {
        sp.edit {
            putString(KEY_ELDERNAME, barrierInfo.eldername)
                .putFloat(KEY_LAT, barrierInfo.lat.toFloat())
                .putFloat(KEY_LON, barrierInfo.lon.toFloat())
                .putFloat(KEY_RADIUS, barrierInfo.radius.toFloat())
        }
        Log.d(TAG, "BarrierInfo saved to local")
    }

    fun getSavedBarrierInfo(): BarrierInfo? {
        val eldername = sp.getString(KEY_ELDERNAME, null) ?: return null
        val lat = sp.getFloat(KEY_LAT, 0f).toDouble()
        val lon = sp.getFloat(KEY_LON, 0f).toDouble()
        val radius = sp.getFloat(KEY_RADIUS, 0f).toDouble()

        if (lat == 0.0 && lon == 0.0) {
            return null
        }
        return BarrierInfo(eldername, lon, lat, radius)
    }

    fun removeAllGeofences() {
        geoFenceClient.removeGeoFence()
        currentFenceId = null
        clearLocalStorage()
        Log.d(TAG, "All geofences removed")
    }

    fun removeGeofence(customId: String) {
        geoFenceClient.removeGeoFence()
        if (currentFenceId == customId) {
            currentFenceId = null
        }
        Log.d(TAG, "Geofence removed: $customId")
    }

    private fun clearLocalStorage() {
        sp.edit { clear() }
        Log.d(TAG, "Local storage cleared")
    }
}

package geofence.manager

import android.content.Context
import android.util.Log
import com.amap.api.fence.GeoFenceClient
import com.amap.api.fence.GeoFenceListener
import com.amap.api.location.DPoint
import com.example.common.geofence.model.BarrierInfo

class CognitiveGeofenceManager(context: Context) {

    companion object {
        private const val TAG = "CognitiveGeofenceMgr"
        const val GEOFENCE_BROADCAST_ACTION = "com.location.apis.geofence.broadcast"
    }

    private val geoFenceClient: GeoFenceClient = GeoFenceClient(context)

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

    /**
     * 根据服务器下发的 BarrierInfo 创建本地围栏
     * @param barrierInfo 服务器返回的围栏配置
     * @return true = 创建成功，false = 重复创建或其他失败
     */
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
        Log.d(TAG, "Geofence created: eldername=$customId, lat=${barrierInfo.lat}, lon=${barrierInfo.lon}, radius=$radius")
        return true
    }

    /**
     * 删除所有围栏
     */
    fun removeAllGeofences() {
        geoFenceClient.removeGeoFence()
        currentFenceId = null
        Log.d(TAG, "All geofences removed")
    }

    /**
     * 删除指定围栏
     */
    fun removeGeofence(customId: String) {
        geoFenceClient.removeGeoFence()
        if (currentFenceId == customId) {
            currentFenceId = null
        }
        Log.d(TAG, "Geofence removed: $customId")
    }
}

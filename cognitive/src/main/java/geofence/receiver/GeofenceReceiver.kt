package geofence.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.amap.api.fence.GeoFence
import com.amap.api.fence.GeoFenceClient
import com.example.common.geofence.GeofenceConstants
import com.example.common.geofence.model.ElderMovement
import com.example.common.persistense.geofence.GeofenceItem
import com.example.common.persistense.geofence.GeofenceRepository
import geofence.network.ElderMovementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "GeofenceReceiver"
        const val GEOFENCE_BROADCAST_ACTION = GeofenceConstants.GEOFENCE_BROADCAST_ACTION
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        if (action != GEOFENCE_BROADCAST_ACTION) return

        val bundle = intent.extras ?: return

        val status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS)
        val customId = bundle.getString(GeoFence.BUNDLE_KEY_CUSTOMID) ?: return
        val fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID)
        val fence = bundle.getParcelable(GeoFence.BUNDLE_KEY_FENCE, GeoFence::class.java)

        saveGeofenceEvent(context, status, customId, fence)
    }

    private fun saveGeofenceEvent(
        context: Context,
        amapStatus: Int,
        customId: String,
        fence: GeoFence?
    ) {
        try {
            GeofenceRepository.initialize(context)

            val localStatus = mapAmapStatusToLocal(amapStatus)

            var lat = 0.0
            var lng = 0.0
            if (fence?.center != null) {
                val center = fence.center
                lat = center.latitude
                lng = center.longitude
            }

            val minutes = (System.currentTimeMillis() / 1000 / 60).toInt()

            val item = GeofenceItem(
                id = 0,
                timestamp = minutes,
                lat = lat,
                lng = lng,
                status = localStatus
            )

            GeofenceRepository.insertEventBlocking(item)
            Log.d(TAG, "Geofence event saved: status=$localStatus, customId=$customId")

            // 上报事件到后端
            reportElderMovement(customId, lng, lat, localStatus)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save geofence event", e)
        }
    }

    private fun reportElderMovement(childname: String, lng: Double, lat: Double, status: Int) {
        val statusStr = mapLocalStatusToString(status)
        val time = System.currentTimeMillis()

        val movement = ElderMovement(
            childname = childname,
            lon = lng,
            lat = lat,
            time = time,
            status = statusStr
        )

        CoroutineScope(Dispatchers.Main).launch {
            ElderMovementRepository.postElderMovement(childname, movement).collect { result ->
                result.onSuccess {
                    Log.d(TAG, "ElderMovement reported successfully")
                }.onFailure { e ->
                    Log.e(TAG, "Failed to report ElderMovement: ${e.message}")
                }
            }
        }
    }

    private fun mapAmapStatusToLocal(amapStatus: Int): Int = when (amapStatus) {
        GeoFenceClient.GEOFENCE_IN -> GeofenceItem.STATUS_IN
        GeoFenceClient.GEOFENCE_OUT -> GeofenceItem.STATUS_OUT
        GeoFenceClient.GEOFENCE_STAYED -> GeofenceItem.STATUS_STAYED
        else -> GeofenceItem.STATUS_UNKNOWN
    }

    private fun mapLocalStatusToString(status: Int): String = when (status) {
        GeofenceItem.STATUS_IN -> "IN"
        GeofenceItem.STATUS_OUT -> "OUT"
        GeofenceItem.STATUS_STAYED -> "STAYED"
        GeofenceItem.STATUS_LOCAL -> "LOCAL"
        else -> "UNKNOWN"
    }
}
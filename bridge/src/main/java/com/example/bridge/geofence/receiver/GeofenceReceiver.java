package com.example.bridge.geofence.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.location.DPoint;
import persistense.geofence.GeofenceRepository;
import persistense.geofence.GeofenceItem;

import java.util.Calendar;

public class GeofenceReceiver extends BroadcastReceiver {

    public static final String TAG = "GeofenceReceiver";

    public static final String GEOFENCE_BROADCAST_ACTION = "com.location.apis.geofence.broadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (!GEOFENCE_BROADCAST_ACTION.equals(action)) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        // 获取围栏行为：
        int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
        // 获取自定义的围栏标识：
        String customId = bundle.getString(GeoFence.BUNDLE_KEY_CUSTOMID);
        // 获取围栏ID:
        String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
        // 获取当前有触发的围栏对象：
        GeoFence fence = bundle.getParcelable(GeoFence.BUNDLE_KEY_FENCE);
//        fence.getFenceId();          // 围栏ID
//        fence.getCustomId();         // 自定义ID
//        fence.getCenter();           // 中心点 LatLng
//        fence.getRadius();           // 半径
//        fence.getType();             // 围栏类型（圆形、多边形等）
//        fence.getStatus();           // 当前状态

        saveGeofenceEvent(context, status, customId, fenceId, fence);

        // TODO: 网络传输
    }

    private void saveGeofenceEvent(Context context, int amapStatus, String customId, String fenceId, GeoFence fence) {
        try {
            GeofenceRepository.initialize(context);

            int localStatus = mapAmapStatusToLocal(amapStatus);

            // 获取坐标
            double lat = 0.0;
            double lng = 0.0;
            if (fence != null && fence.getCenter() != null) {
                DPoint center = fence.getCenter();
                lat = center.getLatitude();
                lng = center.getLongitude();
            }

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int minuteOfDay = hour * 60 + minute;

            GeofenceItem item = new GeofenceItem(
                0,
                minuteOfDay,
                lat,
                lng,
                localStatus
            );

            GeofenceRepository.insertEventBlocking(item);
            Log.d(TAG, "Geofence event saved: status=" + localStatus + ", customId=" + customId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to save geofence event", e);
        }
    }

    private int mapAmapStatusToLocal(int amapStatus) {
        if (amapStatus == GeoFenceClient.GEOFENCE_IN) {
            return GeofenceItem.STATUS_IN;
        } else if (amapStatus == GeoFenceClient.GEOFENCE_OUT) {
            return GeofenceItem.STATUS_OUT;
        } else if (amapStatus == GeoFenceClient.GEOFENCE_STAYED) {
            return GeofenceItem.STATUS_STAYED;
        } else {
            return GeofenceItem.STATUS_UNKNOWN;
        }
    }
}

package com.example.bridge.geofence.feature;

import static com.amap.api.fence.GeoFenceClient.GEOFENCE_IN;
import static com.amap.api.fence.GeoFenceClient.GEOFENCE_OUT;
import static com.amap.api.fence.GeoFenceClient.GEOFENCE_STAYED;

import android.content.Context;

import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.DPoint;
import com.example.common.geofence.GeofenceConstants;

public class GeofenceManager {

    public static final String GEOFENCE_BROADCAST_ACTION = GeofenceConstants.GEOFENCE_BROADCAST_ACTION;

    private GeoFenceClient mGeoFenceClient;

    public GeofenceManager(Context context) {
        mGeoFenceClient = new GeoFenceClient(context);
        // public static final int GEOFENCE_IN      进入地理围栏
        // public static final int GEOFENCE_OUT     退出地理围栏
        // public static final int GEOFENCE_STAYED  停留在地理围栏内10分钟
        mGeoFenceClient.setActivateAction(GEOFENCE_IN|GEOFENCE_OUT|GEOFENCE_STAYED);
        mGeoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);

        // 删除所有围栏 mGeoFenceClient.removeGeoFence();
    }

    public Status CreateKeywordGeofence(Context context, String keyword /* 行政区划关键字 */, String custom) {
        if (custom.equals(GeofenceStatusManager.getFenceCustomId(context))) return Status.REPEATED_CREATION;
        // 关键字合法性校验
        mGeoFenceClient.addGeoFence(keyword, custom);
        return Status.SUCCESS;
    }

    public Status CreateLatLngGeofence(Context context, DPoint latLng, float radius /* 0(3000) - 50000m */, String custom) {
        if (custom.equals(GeofenceStatusManager.getFenceCustomId(context))) return Status.REPEATED_CREATION;
        mGeoFenceClient.addGeoFence(latLng, radius, custom);
        return Status.SUCCESS;
    }

    public void setFenceListener(GeoFenceListener listener) { // 围栏创建回调
        mGeoFenceClient.setGeoFenceListener(listener);
    }

    public enum Status {
        REPEATED_CREATION, // 重复创建
        SUCCESS,
        FAILURE,
    }
}

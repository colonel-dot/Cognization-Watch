package com.example.bridge.geofence.feature;

import static com.amap.api.fence.GeoFenceClient.GEOFENCE_IN;
import static com.amap.api.fence.GeoFenceClient.GEOFENCE_OUT;
import static com.amap.api.fence.GeoFenceClient.GEOFENCE_STAYED;

import android.content.Context;

import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.DPoint;

public class GeofenceManager {

    public static final String GEOFENCE_BROADCAST_ACTION = "com.location.apis.geofence.broadcast";

    private GeoFenceClient mGeoFenceClient;
    private String id = ""; // 本地化

    public GeofenceManager(Context context) {
        mGeoFenceClient = new GeoFenceClient(context);
        // public static final int GEOFENCE_IN      进入地理围栏
        // public static final int GEOFENCE_OUT     退出地理围栏
        // public static final int GEOFENCE_STAYED  停留在地理围栏内10分钟
        mGeoFenceClient.setActivateAction(GEOFENCE_IN|GEOFENCE_OUT|GEOFENCE_STAYED);
        mGeoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);

        // 删除所有围栏 mGeoFenceClient.removeGeoFence();
    }

    public Status CreateKeywordGeofence(String keyword /* 行政区划关键字 */, String custom) {
        if (custom.equals(id)) return Status.REPEATED_CREATION;
        // 关键字合法性校验
        mGeoFenceClient.addGeoFence(keyword, custom);
        id = custom;
        return Status.SUCCESS;
    }

    public Status CreateLatLngGeofence(DPoint latLng, float radius /* 0 - 50000m */, String custom) {
        if (custom.equals(id)) return Status.REPEATED_CREATION;
        mGeoFenceClient.addGeoFence(latLng, radius, custom);
        id = custom;
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

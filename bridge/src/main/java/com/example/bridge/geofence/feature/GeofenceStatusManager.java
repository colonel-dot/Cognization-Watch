package com.example.bridge.geofence.feature;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 围栏状态管理工具类
 */
public class GeofenceStatusManager {

    private static final String SP_NAME = "geofence_status";
    private static final String KEY_IS_FENCE_ENABLED = "is_fence_enabled";
    private static final String KEY_FENCE_CUSTOM_ID = "fence_custom_id";
    private static final String KEY_FENCE_RADIUS = "fence_radius";
    private static final String KEY_FENCE_LAT = "fence_lat";
    private static final String KEY_FENCE_LNG = "fence_lng";

    private static SharedPreferences getGeofenceSP(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 保存围栏启用状态
     */
    public static void setFenceEnabled(Context context, boolean enabled) {
        getGeofenceSP(context).edit()
                .putBoolean(KEY_IS_FENCE_ENABLED, enabled)
                .apply();
    }

    /**
     * 查询围栏是否已启用
     */
    public static boolean isFenceEnabled(Context context) {
        return getGeofenceSP(context).getBoolean(KEY_IS_FENCE_ENABLED, false);
    }

    /**
     * 保存围栏信息
     */
    public static void saveFenceInfo(Context context, String customId, float radius, double lat, double lng) {
        getGeofenceSP(context).edit()
                .putString(KEY_FENCE_CUSTOM_ID, customId)
                .putFloat(KEY_FENCE_RADIUS, radius)
                .putFloat(KEY_FENCE_LAT, (float) lat)
                .putFloat(KEY_FENCE_LNG, (float) lng)
                .apply();
    }

    /**
     * 获取围栏自定义ID
     */
    public static String getFenceCustomId(Context context) {
        return getGeofenceSP(context).getString(KEY_FENCE_CUSTOM_ID, "");
    }

    /**
     * 获取围栏半径
     */
    public static float getFenceRadius(Context context) {
        return getGeofenceSP(context).getFloat(KEY_FENCE_RADIUS, 3000f);
    }

    /**
     * 获取围栏中心纬度
     */
    public static float getFenceLat(Context context) {
        return getGeofenceSP(context).getFloat(KEY_FENCE_LAT, 0f);
    }

    /**
     * 获取围栏中心经度
     */
    public static float getFenceLng(Context context) {
        return getGeofenceSP(context).getFloat(KEY_FENCE_LNG, 0f);
    }

    /**
     * 清除围栏状态
     */
    public static void clearFenceInfo(Context context) {
        getGeofenceSP(context).edit().clear().apply();
    }
}

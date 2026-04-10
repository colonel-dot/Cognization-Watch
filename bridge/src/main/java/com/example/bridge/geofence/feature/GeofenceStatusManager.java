package com.example.bridge.geofence.feature;

import android.content.Context;
import android.content.SharedPreferences;

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

    public static void setFenceEnabled(Context context, boolean enabled) {
        getGeofenceSP(context).edit()
                .putBoolean(KEY_IS_FENCE_ENABLED, enabled)
                .apply();
    }

    public static boolean isFenceEnabled(Context context) {
        return getGeofenceSP(context).getBoolean(KEY_IS_FENCE_ENABLED, false);
    }

    public static void saveFenceInfo(Context context, String customId, float radius, double lat, double lng) {
        getGeofenceSP(context).edit()
                .putString(KEY_FENCE_CUSTOM_ID, customId)
                .putFloat(KEY_FENCE_RADIUS, radius)
                .putFloat(KEY_FENCE_LAT, (float) lat)
                .putFloat(KEY_FENCE_LNG, (float) lng)
                .apply();
    }

    public static String getFenceCustomId(Context context) {
        return getGeofenceSP(context).getString(KEY_FENCE_CUSTOM_ID, "");
    }

    public static float getFenceRadius(Context context) {
        return getGeofenceSP(context).getFloat(KEY_FENCE_RADIUS, 3000f);
    }

    public static float getFenceLat(Context context) {
        return getGeofenceSP(context).getFloat(KEY_FENCE_LAT, 0f);
    }

    public static float getFenceLng(Context context) {
        return getGeofenceSP(context).getFloat(KEY_FENCE_LNG, 0f);
    }

    public static void clearFenceInfo(Context context) {
        getGeofenceSP(context).edit().clear().apply();
    }
}

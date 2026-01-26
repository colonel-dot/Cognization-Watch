package com.example.bridge.domain.location;

import android.util.Log;

import com.example.bridge.feature.location.LocationFeature;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p> 地理围栏规则评估器
 * <p> 用于根据围栏状态历史判断用户是否处于走失状态
 */
public class GeofenceRuleEvaluator {

    private static final String TAG = "GeofenceRuleEvaluator";
    private static final double LOST_THRESHOLD_MINUTES =  30; // 走失时间阈值

    /**
     * <p> 根据地理围栏状态历史记录评估用户是否走失
     * <p> 由 {@link LocationFeature} 回调
     * <p> @param history 地理围栏状态历史记录
     */
    public boolean isLost(List<GeofenceStatus> history) {
        Log.d(TAG, "开始评估用户是否走失，历史记录条数：" + (history != null ? history.size() : 0));

        if (history == null || history.isEmpty()) {
            Log.d(TAG, "围栏状态历史为空，判定用户未走失");
            return false;
        }

        GeofenceStatus latestStatus = history.get(history.size() - 1);

        // 只要最新状态是围栏外就立即判定为走失
        if (!latestStatus.isInside()) {
            Log.i(TAG, "检测到最新状态为围栏外，立即判定为疑似走失");
            return true;
        }

        Log.d(TAG, "最新状态为在围栏内，判定用户未走失");
        return false;
    }
}

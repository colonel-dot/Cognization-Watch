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
    private static final double LOST_THRESHOLD_MINUTES = 0.001; // 走失时间阈值

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
        if (latestStatus.isInside()) {
            Log.d(TAG, "最新状态为在围栏内，判定用户未走失");
            return false;
        }

        Log.d(TAG, "最新状态为在围栏外，开始计算连续在围栏外的时间");

        long timeOutsideMs = 0;

        // 从最新状态开始向前遍历历史记录
        for (int i = history.size() - 1; i >= 0; i--) {
            GeofenceStatus currentStatus = history.get(i);

            // 找到最近一次在围栏内的状态
            if (currentStatus.isInside()) {
                // 用户第一次离开围栏的状态是下一个记录
                if (i + 1 < history.size()) {
                    GeofenceStatus firstOutsideStatus = history.get(i + 1);
                    timeOutsideMs = latestStatus.timestamp().getTime()
                            - firstOutsideStatus.timestamp().getTime();
                    Log.d(TAG, "检测到从围栏内到围栏外的状态切换，首次离开围栏时间："
                            + firstOutsideStatus.timestamp());
                }
                break;
            }

            // 如果遍历到最早的一条记录且全部都在围栏外
            if (i == 0) {
                timeOutsideMs = latestStatus.timestamp().getTime()
                        - currentStatus.timestamp().getTime();
                Log.d(TAG, "历史记录中全部为围栏外状态，最早时间点："
                        + currentStatus.timestamp());
            }
        }

        double timeOutsideMinutes =
                TimeUnit.MILLISECONDS.toSeconds(timeOutsideMs) / 60.0;

        Log.d(TAG, "连续在围栏外的时间：" + timeOutsideMinutes
                + " 分钟 " + timeOutsideMs + " 毫秒");
        Log.d(TAG, "走失判定阈值：" + LOST_THRESHOLD_MINUTES + " 分钟");

        boolean isLost = timeOutsideMinutes >= LOST_THRESHOLD_MINUTES;
        Log.i(TAG, "评估结果：用户是否走失 = " + isLost);

        return isLost;
    }
}

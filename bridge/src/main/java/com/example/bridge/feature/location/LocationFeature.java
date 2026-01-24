package com.example.bridge.feature.location;

import android.util.Log;

import com.example.bridge.domain.location.GeofenceRuleEvaluator;
import com.example.bridge.domain.location.GeofenceStatus;
import com.example.bridge.integration.location.GeofenceAlarmAdapter;
import com.example.bridge.integration.location.GeofenceSdkAdapter;
import com.example.bridge.model.location.GeofenceEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p> 负责接收和维护围栏事件序列的位置能力调度实现类
 * <p> 持有 {@link GeofenceSdkAdapter} 和 {@link GeofenceRuleEvaluator} 类实例
 * <p> 通过 {@link GeofenceRuleEvaluator} 类判断是否进入走失状态, 最终将状态变化回调通知给显示层监听者
 * <p> 自身接收 {@link GeofenceAlarmAdapter} 回调的围栏事件信息
 */
public class LocationFeature implements GeofenceAlarmAdapter.OnGeofenceEventListener {

    private static final String TAG = "LocationFeature";

    private final GeofenceSdkAdapter geofenceSdkAdapter;
    private final GeofenceRuleEvaluator ruleEvaluator;

    // 围栏内外状态的历史快照序列，用于规则判断
    private final List<GeofenceStatus> geofenceStatusHistory = new ArrayList<>();

    // 由显示层设置的走失状态变化监听器
    private LostStateListener lostStateListener;

    // 判断当前是否已处于走失状态以区分过渡态
    private boolean currentlyLost = false;

    public LocationFeature(GeofenceSdkAdapter geofenceSdkAdapter,
                           GeofenceRuleEvaluator ruleEvaluator) {
        this.geofenceSdkAdapter = geofenceSdkAdapter;
        this.ruleEvaluator = ruleEvaluator;

        // 注册围栏事件监听
        this.geofenceSdkAdapter.setListener(this); // to GeofenceAlarmAdapter
    }

    /**
     * <p> 获得当前是否处于走失状态的中间方法
     * <p> 由 {@link GeofenceAlarmAdapter} 回调
     */
    @Override
    public void onGeofenceEvent(GeofenceEvent event) {
        boolean isInside = event == GeofenceEvent.ENTER;
        Log.d(TAG, "记录新的状态快照：" + event);

        geofenceStatusHistory.add(
                new GeofenceStatus(isInside, new Date())
        );

        boolean isNowLost = ruleEvaluator.isLost(geofenceStatusHistory);

        if (isNowLost && !currentlyLost) {
            currentlyLost = true;

            if (lostStateListener != null) {
                Log.i(TAG, "通知显示层监听器：判定为走失状态");
                lostStateListener.onLostStateChanged(true);
            }

        } else if (!isNowLost && currentlyLost) {
            currentlyLost = false;

            if (lostStateListener != null) {
                Log.i(TAG, "通知显示层监听器：走失状态解除");
                lostStateListener.onLostStateChanged(false);
            }
        }
    }

    /**
     * 设置走失状态变化监听器
     */
    public void setLostStateListener(LostStateListener lostStateListener) {
        this.lostStateListener = lostStateListener;
    }

    /**
     * <p> 由显示层实现的走失状态回调接口
     * <p> 用于接收走失状态变化通知
     */
    public interface LostStateListener {

        /**
         * 走失状态发生变化时回调
         */
        void onLostStateChanged(boolean isLost);
    }
}

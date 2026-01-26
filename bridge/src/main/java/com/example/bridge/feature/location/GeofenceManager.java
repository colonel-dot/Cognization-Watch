package com.example.bridge.feature.location;

import android.util.Log;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.example.bridge.domain.location.GeofenceRuleEvaluator;
import com.example.bridge.integration.location.GeofenceSdkAdapter;
import com.example.bridge.model.location.GeofenceInfo;

/**
 * <p> 向显示层暴露稳定 API、对外统一的地理围栏管理入口
 * <p> 持有 {@link LocationFeature} 和 {@link GeofenceSdkAdapter} 类实例
 * <p> 自身接收 {@link GeofenceSdkAdapter} 回调的轨迹采集启动成功信息
 */
public class GeofenceManager implements GeofenceSdkAdapter.OnTraceStatusListener {

    private static final String TAG = "GeofenceManager";

    private final LocationFeature locationFeature;
    private final GeofenceSdkAdapter geofenceSdkAdapter;
    private OnTraceReadyListener traceReadyListener;

    public interface OnTraceReadyListener {
        void onTraceReady();
    }

    public GeofenceManager(LBSTraceClient client, Trace trace) {
        Log.d(
                TAG,
                "初始化 GeofenceManager: serviceId = "
                        + trace.getServiceId()
                        + " entityName = "
                        + trace.getEntityName()
        );

        this.geofenceSdkAdapter = new GeofenceSdkAdapter(client, trace);
        this.geofenceSdkAdapter.setTraceStatusListener(this);
        this.locationFeature = new LocationFeature(
                geofenceSdkAdapter,
                new GeofenceRuleEvaluator()
        );
    }

    public void setTraceReadyListener(OnTraceReadyListener listener) {
        this.traceReadyListener = listener;
    }

    @Override
    public void onStartGatherSuccess() {
        Log.i(TAG, "轨迹采集已就绪，通知上层监听者。");
        if (traceReadyListener != null) {
            traceReadyListener.onTraceReady();
        }
    }

    public void createCircularFence(GeofenceInfo info) {
        Log.d(
                TAG,
                "创建圆形围栏：fenceId = "
                        + info.getFenceId()
                        + " fenceName = "
                        + info.getFenceName()
        );
        geofenceSdkAdapter.createCircularFence(info);
    }

    public void createDistrictFence(GeofenceInfo info) {
        Log.d(
                TAG,
                "创建行政区围栏：fenceId = "
                        + info.getFenceId()
                        + " keyword = "
                        + info.getKeyword()
        );
        geofenceSdkAdapter.createDistrictFence(info);
    }

    public void deleteFence(GeofenceInfo info) {
        Log.d(TAG, "删除围栏：fenceId = " + info.getFenceId());
        geofenceSdkAdapter.deleteFence(info);
    }

    public void start() {
        Log.d(TAG, "启动地理围栏服务");
        geofenceSdkAdapter.start();
    }

    public void stop() {
        Log.d(TAG, "停止地理围栏服务");
        geofenceSdkAdapter.stop();
    }

    public void setLostStateListener(LocationFeature.LostStateListener listener) {
        Log.d(TAG, "设置走失状态监听器");
        locationFeature.setLostStateListener(listener);
    }
}

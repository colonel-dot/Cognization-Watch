package com.example.bridge.integration.location;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.entity.AddEntityRequest;
import com.baidu.trace.api.entity.AddEntityResponse;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.fence.CreateFenceRequest;
import com.baidu.trace.api.fence.DeleteFenceRequest;
import com.baidu.trace.api.fence.MonitoredStatusRequest;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;
import com.example.bridge.model.location.GeofenceInfo;

import java.util.Collections;
import java.util.List;

/**
 * <p> 百度地图鹰眼 SDK 适配器
 * <p> 负责对接 LBSTraceClient，屏蔽 SDK 的接口、回调与线程模型差异，并将 SDK 事件转交给上层业务模块
 * <p> 持有 {@link GeofenceAlarmAdapter} 类实例
 */
public class GeofenceSdkAdapter {

    private static final String TAG = "GeofenceSdkAdapter";

    private final LBSTraceClient client;
    private final Trace trace;

    // SDK 请求 tag，自增以区分请求
    private int tag = 1;

    private final GeofenceAlarmAdapter listenerAdapter = new GeofenceAlarmAdapter();

    private OnTraceStatusListener traceStatusListener; // to GeofenceManager
    private OnTraceListener traceListener;

    /**
     * 轨迹采集状态监听接口
     */
    public interface OnTraceStatusListener {
        /**
         * 轨迹采集启动成功回调
         */
        void onStartGatherSuccess();
    }

    public void setTraceStatusListener(OnTraceStatusListener listener) {
        this.traceStatusListener = listener;
    }

    public GeofenceSdkAdapter(LBSTraceClient client, Trace trace) {
        this.trace = trace;
        this.client = client;
    }

    public void createCircularFence(GeofenceInfo info) {
        CreateFenceRequest request = CreateFenceRequest.buildServerCircleRequest(
                tag++,
                trace.getServiceId(),
                info.getFenceName(),
                info.getMonitoredPerson(),
                info.getCenter(),
                info.getRadius(),
                200, // 去噪半径
                CoordType.bd09ll
        );
        Log.d(TAG, "创建圆形围栏请求：" + request);
        client.createFence(request, listenerAdapter);
    }

    public void queryMonitoredStatus(GeofenceInfo info) {
        // 此功能已根据用户要求移除，但保留方法以备将来使用
        Log.w(TAG, "queryMonitoredStatus 方法已被禁用");
    }

    public void createDistrictFence(GeofenceInfo info) {
        CreateFenceRequest request = CreateFenceRequest.buildServerDistrictRequest(
                tag++,
                trace.getServiceId(),
                info.getFenceName(),
                info.getMonitoredPerson(),
                info.getKeyword(),
                200
        );
        client.createFence(request, listenerAdapter);
    }

    public void deleteFence(GeofenceInfo info) {
        DeleteFenceRequest request = DeleteFenceRequest.buildServerRequest(
                tag++,
                trace.getServiceId(),
                info.getMonitoredPerson(),
                Collections.singletonList(info.getFenceId())
        );
        client.deleteFence(request, listenerAdapter);
    }

    /**
     * 设置围栏事件监听器
     */
    public void setListener(GeofenceAlarmAdapter.OnGeofenceEventListener listener) {
        listenerAdapter.setListener(listener);
    }

    public void start() {
        Log.d(TAG, "开始启动轨迹服务和采集");

        traceListener = new OnTraceListener() {

            @Override
            public void onBindServiceCallback(int status, String message) {
                Log.d(TAG, "绑定服务回调：status = " + status + " message = " + message);
            }

            @Override
            public void onStartTraceCallback(int status, String message) {
                Log.d(TAG, "启动轨迹服务回调：status = " + status + " message = " + message);
                 if (status == 0) {
                    // 服务启动成功后，立即启动采集
                    client.startGather(traceListener);
                }
            }

            @Override
            public void onStopTraceCallback(int status, String message) {
                Log.d(TAG, "停止轨迹服务回调：status = " + status + " message = " + message);
            }

            @Override
            public void onStartGatherCallback(int status, String message) {
                Log.d(TAG, "开始采集回调：status = " + status + " message = " + message);
                if (status == 0) {
                    Log.i(TAG, "【成功】轨迹采集已成功启动，准备通知上层");
                    if (traceStatusListener != null) {
                        traceStatusListener.onStartGatherSuccess();
                    }
                }
            }

            @Override
            public void onStopGatherCallback(int status, String message) {
                Log.d(TAG, "停止采集回调：status = " + status + " message = " + message);
            }

            @Override
            public void onPushCallback(byte messageType, PushMessage pushMessage) {
                Log.i(
                        TAG,
                        "【收到推送】messageType = " + messageType +
                                " 内容 = " + pushMessage.getMessage()
                );
                listenerAdapter.onPushMessage(pushMessage);
            }

            @Override
            public void onInitBOSCallback(int status, String message) {
                Log.d(TAG, "BOS 初始化回调：status = " + status + " message = " + message);
            }

            @Override
            public void onTraceDataUploadCallBack(int status,
                                                  String message,
                                                  int totalPoints,
                                                  int successPoints) {
                Log.d(
                        TAG,
                        "【数据上传】status = " + status +
                                ", message = " + message +
                                ", total = " + totalPoints +
                                ", success = " + successPoints
                );
            }
        };

        // 只启动服务，采集在 onStartTraceCallback 成功后启动
        client.startTrace(trace, traceListener);
    }

    public void stop() {
        Log.d(TAG, "停止采集和轨迹服务");
        client.stopGather(traceListener);
        client.stopTrace(trace, null);
        traceListener = null;
    }
}

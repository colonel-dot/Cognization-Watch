package com.example.bridge.integration.location;

import android.util.Log;

import com.baidu.trace.api.fence.AddMonitoredPersonResponse;
import com.baidu.trace.api.fence.CreateFenceResponse;
import com.baidu.trace.api.fence.DeleteFenceResponse;
import com.baidu.trace.api.fence.DeleteMonitoredPersonResponse;
import com.baidu.trace.api.fence.FenceAlarmPushInfo;
import com.baidu.trace.api.fence.FenceListResponse;
import com.baidu.trace.api.fence.HistoryAlarmResponse;
import com.baidu.trace.api.fence.ListMonitoredPersonResponse;
import com.baidu.trace.api.fence.MonitoredAction;
import com.baidu.trace.api.fence.MonitoredStatusByLocationResponse;
import com.baidu.trace.api.fence.MonitoredStatusResponse;
import com.baidu.trace.api.fence.OnFenceListener;
import com.baidu.trace.api.fence.UpdateFenceResponse;
import com.baidu.trace.model.PushMessage;
import com.example.bridge.feature.location.LocationFeature;
import com.example.bridge.model.location.GeofenceEvent;

/**
 * <p> 用于将 SDK 返回的围栏相关回调事件统一转换为本地定义的 GeofenceEvent
 */
public class GeofenceAlarmAdapter implements OnFenceListener {

    private static final String TAG = "GeofenceAlarmAdapter";

    @Override
    public void onCreateFenceCallback(CreateFenceResponse createFenceResponse) {
        Log.d(TAG, "创建围栏回调: " + createFenceResponse);
    }

    @Override
    public void onUpdateFenceCallback(UpdateFenceResponse updateFenceResponse) {
        Log.d(TAG, "更新围栏回调: " + updateFenceResponse);
    }

    @Override
    public void onDeleteFenceCallback(DeleteFenceResponse deleteFenceResponse) {
        Log.d(TAG, "删除围栏回调: " + deleteFenceResponse);
    }

    @Override
    public void onFenceListCallback(FenceListResponse fenceListResponse) {
        Log.d(TAG, "获取围栏列表回调: " + fenceListResponse);
    }

    @Override
    public void onMonitoredStatusCallback(MonitoredStatusResponse response) {
        Log.d(TAG, "监控对象状态回调: " + response);
    }

    @Override
    public void onMonitoredStatusByLocationCallback(
            MonitoredStatusByLocationResponse monitoredStatusByLocationResponse) {
        Log.d(TAG, "基于位置的监控状态回调: " + monitoredStatusByLocationResponse);
    }

    @Override
    public void onHistoryAlarmCallback(HistoryAlarmResponse historyAlarmResponse) {
        Log.d(TAG, "历史围栏报警回调: " + historyAlarmResponse);
    }

    @Override
    public void onAddMonitoredPersonCallback(AddMonitoredPersonResponse addMonitoredPersonResponse) {
        Log.d(TAG, "添加监控对象回调: " + addMonitoredPersonResponse);
    }

    @Override
    public void onDeleteMonitoredPersonCallback(
            DeleteMonitoredPersonResponse deleteMonitoredPersonResponse) {
        Log.d(TAG, "删除监控对象回调: " + deleteMonitoredPersonResponse);
    }

    @Override
    public void onListMonitoredPersonCallback(
            ListMonitoredPersonResponse listMonitoredPersonResponse) {
        Log.d(TAG, "获取监控对象列表回调: " + listMonitoredPersonResponse);
    }

    /**
     * 围栏事件监听器，由 {@link LocationFeature} 业务层实现
     */
    public interface OnGeofenceEventListener {
        void onGeofenceEvent(GeofenceEvent event);
    }

    private OnGeofenceEventListener listener;

    public void setListener(OnGeofenceEventListener listener) {
        this.listener = listener;
    }

    /**
     * 处理围栏报警的推送消息 (被动接收)
     * 由 OnTraceListener#onPushCallback 调用
     */
    public void onPushMessage(PushMessage msg) {
        if (msg == null) {
            return;
        }

        FenceAlarmPushInfo alarmInfo = msg.getFenceAlarmPushInfo();
        if (alarmInfo == null) {
            Log.w(TAG, "收到围栏报警推送但报警信息为空");
            return;
        }

        Log.d(TAG, "处理围栏报警推送消息: " + msg.getMessage());

        MonitoredAction action = alarmInfo.getMonitoredAction();
        GeofenceEvent event = null;

        if (action == MonitoredAction.enter) {
            event = GeofenceEvent.ENTER;
        } else if (action == MonitoredAction.exit) {
            event = GeofenceEvent.EXIT;
        }

        if (event != null && listener != null) {
            Log.i(TAG, "获得围栏事件并分发: " + event);
            listener.onGeofenceEvent(event);
        } else {
            Log.w(
                    TAG,
                    "未分发推送事件，action = " + action
                            + " listener = "
                            + (listener == null ? "空" : "非空")
            );
        }
    }
}

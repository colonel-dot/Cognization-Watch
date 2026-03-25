package com.example.common.rtc;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.common.util.GenerateTestUserSig;
import com.example.common.util.StringMap;
import com.tencent.liteav.device.TXDeviceManager;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import java.lang.ref.WeakReference;

public class RtcManager {
    private static final String TAG = "RtcManager";

    private static final int SDKAPPID = 1600115028;

    private TRTCCloud cloud;
    private TXDeviceManager manager;
    private Context context;

    private String userId;
    private String targetId;
    private boolean isElder;
    private WeakReference<TXCloudVideoView> localVideoViewRef;
    private WeakReference<TXCloudVideoView> remoteVideoViewRef;

    public RtcManager(Context context, String userId, String targetId, Boolean isElder) {
        this.context = context.getApplicationContext();
        this.userId = userId;
        this.targetId = targetId;
        this.isElder = isElder;
    }

    public void initCloud(TRTCCloud cloud) {
        if (this.cloud == null) {
            this.cloud = cloud;
            manager = cloud.getDeviceManager();
            manager.enableCameraAutoFocus(true);
            setupInternalListener();
        }
    }

    public void startVideoCall() {
        ensureCloudInitialized();
        Log.d(TAG, "startVideoCall - userId: " + userId + ", targetId: " + targetId + ", isElder: " + isElder);
        TRTCCloudDef.TRTCParams trtcParams = new TRTCCloudDef.TRTCParams();
        trtcParams.sdkAppId = SDKAPPID;
        trtcParams.userId = userId;
        trtcParams.userSig = GenerateTestUserSig.genTestUserSig(trtcParams.userId);
        if (!isElder) {
            trtcParams.roomId = StringMap.mapStringToSixDigitInt(userId + targetId); // * roomId = children userId + elder targetId
        } else {
            trtcParams.roomId = StringMap.mapStringToSixDigitInt(targetId + userId);
        }
        Log.d(TAG, "TRTC params - sdkAppId: " + trtcParams.sdkAppId + ", roomId: " + trtcParams.roomId);
        // 设置本地预览渲染参数
        TRTCCloudDef.TRTCRenderParams trtcRenderParams = new TRTCCloudDef.TRTCRenderParams();
        trtcRenderParams.fillMode   = TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL;
        trtcRenderParams.mirrorType = TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_AUTO;
        trtcRenderParams.rotation = TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
        if (cloud != null) {
            cloud.setLocalRenderParams(trtcRenderParams);
            cloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_LIVE);
        }
    }

    public void startVideoCall(String userId, int roomId) {
        ensureCloudInitialized();
        TRTCCloudDef.TRTCParams trtcParams = new TRTCCloudDef.TRTCParams();
        trtcParams.sdkAppId = SDKAPPID;
        trtcParams.userId = userId;
        trtcParams.userSig = GenerateTestUserSig.genTestUserSig(userId);
        trtcParams.roomId = roomId;

        TRTCCloudDef.TRTCRenderParams trtcRenderParams = new TRTCCloudDef.TRTCRenderParams();
        trtcRenderParams.fillMode   = TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL;
        trtcRenderParams.mirrorType = TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_AUTO;
        trtcRenderParams.rotation = TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
        if (cloud != null) {
            cloud.setLocalRenderParams(trtcRenderParams);
            cloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_LIVE);
        }
    }

    public void switchCamera() {
        if (manager != null) {
            if(manager.isFrontCamera()) {
                manager.switchCamera(false);
            } else {
                manager.switchCamera(true); // true -> front
            }
        }
    }

    private void ensureCloudInitialized() {
        if (cloud == null && context != null) {
            cloud = TRTCCloud.sharedInstance(context);
            manager = cloud.getDeviceManager();
            manager.enableCameraAutoFocus(true);
            setupInternalListener();
        }
    }

    private void setupInternalListener() {
        if (cloud == null) return;
        cloud.addListener(new TRTCCloudListener() {
            @Override
            public void onError(int errCode, String errMsg, Bundle extraInfo) {
                Log.e(TAG, "TRTC onError - errCode: " + errCode + ", errMsg: " + errMsg);
                Toast.makeText(context, "视频通话初始化出现错误（错误码：" + errCode + ")", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onEnterRoom(long result) {
                Log.d(TAG, "onEnterRoom - result: " + result);
                if (result > 0) {
                    Log.d(TAG, "房间进入成功");
                } else {
                    Log.d(TAG, "房间进入失败");
                    Toast.makeText(context, "房间进入失败，错误码：" + result, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onExitRoom(int reason) {
                Log.d(TAG, "已退出房间");
            }

            @Override
            public void onUserVideoAvailable(String userId, boolean available) {
                Log.d(TAG, "对方视频流准备完成 开始推流");
                if (remoteVideoViewRef != null && remoteVideoViewRef.get() != null) {
                    startRemoteView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, remoteVideoViewRef.get());
                }
                if (localVideoViewRef != null && localVideoViewRef.get() != null) {
                    startLocalPreview(true, localVideoViewRef.get());
                }
            }
        });
    }

    public void setVideoViews(TXCloudVideoView localView, TXCloudVideoView remoteView) {
        this.localVideoViewRef = new WeakReference<>(localView);
        this.remoteVideoViewRef = new WeakReference<>(remoteView);
    }

    public void startLocalPreview(boolean frontCamera, TXCloudVideoView view) {
        ensureCloudInitialized();
        if (cloud != null && view != null) {
            cloud.startLocalPreview(frontCamera, view);
        }
    }

    public void startRemoteView(String userId, int streamType, TXCloudVideoView view) {
        ensureCloudInitialized();
        if (cloud != null && view != null) {
            cloud.startRemoteView(userId, streamType, view);
        }
    }

    public void muteLocalAudio(boolean mute) {
        ensureCloudInitialized();
        if (cloud != null) {
            cloud.muteLocalAudio(mute);
        }
    }

    public void exitRoom() {
        if (cloud != null) {
            cloud.exitRoom();
        }
    }

    public void destroy() {
        if (cloud != null) {
            cloud.destroySharedInstance();
            cloud = null;
            manager = null;
        }
    }

    public void addListener(TRTCCloudListener listener) {
        ensureCloudInitialized();
        if (cloud != null) {
            cloud.addListener(listener);
        }
    }
}

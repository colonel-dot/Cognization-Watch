package com.example.bridge.rtc;

import com.example.bridge.util.GenerateTestUserSig;
import com.example.bridge.util.StringMap;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;

public class RtcManager {
    private static final String TAG = "RtcManager";

    private static final int SDKAPPID = 1600115028;

    private TRTCCloud cloud;

    private String userId;

    public RtcManager(String userId) {
        this.userId = userId;
    }

    public void initCloud(TRTCCloud cloud) {
        if (this.cloud == null) this.cloud = cloud;
    }

    public void startVideoCall() {
        TRTCCloudDef.TRTCParams trtcParams = new TRTCCloudDef.TRTCParams();
        trtcParams.sdkAppId = SDKAPPID;
        trtcParams.userId = userId;
        trtcParams.userSig = GenerateTestUserSig.genTestUserSig(trtcParams.userId);
        trtcParams.roomId = StringMap.mapStringToSixDigitInt(userId);

        // 设置本地预览渲染参数
        TRTCCloudDef.TRTCRenderParams trtcRenderParams = new TRTCCloudDef.TRTCRenderParams();
        trtcRenderParams.fillMode   = TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL;
        trtcRenderParams.mirrorType = TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_AUTO;
        trtcRenderParams.rotation = TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
        cloud.setLocalRenderParams(trtcRenderParams);

        cloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_LIVE);
    }

    public void startVideoCall(String userId, int roomId) {
        TRTCCloudDef.TRTCParams trtcParams = new TRTCCloudDef.TRTCParams();
        trtcParams.sdkAppId = SDKAPPID;
        trtcParams.userId = userId;
        trtcParams.userSig = GenerateTestUserSig.genTestUserSig(userId);
        trtcParams.roomId = roomId;

        TRTCCloudDef.TRTCRenderParams trtcRenderParams = new TRTCCloudDef.TRTCRenderParams();
        trtcRenderParams.fillMode   = TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL;
        trtcRenderParams.mirrorType = TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_AUTO;
        trtcRenderParams.rotation = TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
        cloud.setLocalRenderParams(trtcRenderParams);


        cloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_LIVE);
    }
}

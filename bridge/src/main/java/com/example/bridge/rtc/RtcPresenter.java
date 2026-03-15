package com.example.bridge.rtc;

import android.util.Log;

import com.example.bridge.util.GenerateTestUserSig;
import com.example.bridge.util.UserIdGenerate;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;

import org.json.JSONException;
import org.json.JSONObject;

public class RtcPresenter implements RtcContract.Presenter {
    private static final String TAG = "RtcPresenter";

    private static final int SDKAPPID = 1600115028;

    private TRTCCloud cloud;

    private RtcContract.View view;

    RtcPresenter () {
    }

    RtcPresenter (RtcContract.View view) {
        attachView(view);
    }

    @Override
    public void attachView(RtcContract.View view) {
        if (this.view == null) this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    public void initCloud(TRTCCloud cloud) {
        if (this.cloud == null) this.cloud = cloud;
    }

    public void startVideoCall() {
        TRTCCloudDef.TRTCParams trtcParams = new TRTCCloudDef.TRTCParams();
        trtcParams.sdkAppId = SDKAPPID;
        trtcParams.userId = UserIdGenerate.generateRandomString(6); // TODO：服务器配置的唯一ID
        trtcParams.userSig = GenerateTestUserSig.genTestUserSig(trtcParams.userId); // TODO: 由服务器部署计算
        trtcParams.roomId = 123321; // TODO：由服务器分配

        // 设置本地预览渲染参数
        TRTCCloudDef.TRTCRenderParams trtcRenderParams = new TRTCCloudDef.TRTCRenderParams();
        trtcRenderParams.fillMode   = TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FILL;
        trtcRenderParams.mirrorType = TRTCCloudDef.TRTC_VIDEO_MIRROR_TYPE_AUTO;
        trtcRenderParams.rotation = TRTCCloudDef.TRTC_VIDEO_ROTATION_0;
        cloud.setLocalRenderParams(trtcRenderParams);

        view.startLocalPreview(cloud);

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

        view.startLocalPreview(cloud);

        cloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_LIVE);
    }
}

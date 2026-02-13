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
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("api", "setCameraCapturerType");
            JSONObject params = new JSONObject();
            params.put("type", 2); // 1: Camera1, 2: Camera2
            jsonObject.put("params", params);
            cloud.callExperimentalAPI(jsonObject.toString());
            Log.i(TAG, "Force switch to Camera2 API");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (this.cloud == null) this.cloud = cloud;
    }

    public void startVideoCall() {
        TRTCCloudDef.TRTCParams trtcParams = new TRTCCloudDef.TRTCParams();
        trtcParams.sdkAppId = 1600115028;
        trtcParams.userId = UserIdGenerate.generateRandomString(6); // TODO：服务器配置的唯一ID
        trtcParams.userSig = GenerateTestUserSig.genTestUserSig(trtcParams.userId);
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
}

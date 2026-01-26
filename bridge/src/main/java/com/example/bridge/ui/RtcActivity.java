package com.example.bridge.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bridge.R;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

/**
 * 声网样例代码有兼容问题，具体表现是 Agora Camera1 在 Android15 vivo 设备上触发 JNI 层崩溃
 * 根因是 SDK 向 Camera.setVersion() 传入 null 字符串
 */
public class RtcActivity extends AppCompatActivity {

    private static final String TAG = "RtcActivity";

    private static final int PERMISSION_REQ_ID = 22;

    private String appId = "54fdd3029a7441e698794a74148d5a50";
    private String channelName = "bridge";
    private String token = "007eJxTYLj9x7HcTaUqe7fbb9FJR5tL19xu+3fKhD1RznPWK/ZXiW0KDKYmaSkpxgZGlonmJiaGqWaWFuaWJkC2oYlFimmiqcGvtSWZDYGMDGyLHBgZGSAQzGdIKspMSU9lYAAAvnkgSQ==";

    private RtcEngine mRtcEngine;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            runOnUiThread(() ->
                    Toast.makeText(RtcActivity.this, "Join channel success", Toast.LENGTH_SHORT).show()
            );
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() ->
                    Toast.makeText(RtcActivity.this, "User offline: " + uid, Toast.LENGTH_SHORT).show()
            );
        }
    };

    private void initializeAndJoinChannel() {
        Log.d(TAG, "initializeAndJoinChannel");

        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getApplicationContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;

            mRtcEngine = RtcEngine.create(config);

            mRtcEngine.setExternalVideoSource(
                    true,   // enable
                    false,  // useTexture (先用 byte buffer，简单)
                    Constants.ExternalVideoSourceType.VIDEO_FRAME
            );

        } catch (Exception e) {
            Log.e(TAG, "RtcEngine create failed", e);
            return;
        }

        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        options.publishMicrophoneTrack = true;
        options.publishCameraTrack = true;
        options.autoSubscribeAudio = true;
        options.autoSubscribeVideo = true;

        mRtcEngine.joinChannel(token, channelName, 0, options);

        Log.d(TAG, "initializeAndJoinChannel over");
    }

    private void setupRemoteVideo(int uid) {
        Log.d(TAG, "setupRemoteVideo uid=" + uid);

        FrameLayout container = findViewById(R.id.remote_video_view_container);
        SurfaceView remoteView = new SurfaceView(this);
        remoteView.setZOrderMediaOverlay(true);
        container.addView(remoteView);

        mRtcEngine.setupRemoteVideo(
                new VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_FIT, uid)
        );
    }

    private String[] getRequiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };
        }
    }

    private boolean checkPermissions() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtc);

        if (checkPermissions()) {
            getWindow().getDecorView().post(this::initializeAndJoinChannel);
        } else {
            ActivityCompat.requestPermissions(
                    this, getRequiredPermissions(), PERMISSION_REQ_ID
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkPermissions()) {
            getWindow().getDecorView().post(this::initializeAndJoinChannel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
    }
}

package com.example.bridge.rtc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bridge.R;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

/**
 * 声网样例代码有兼容问题，具体表现是 Agora Camera1 在 Android15 vivo 设备上触发 JNI 层崩溃
 * 根因是 SDK 向 Camera.setVersion() 传入 null 字符串
 * 妈的 腾讯云也是这个问题
 * 因为 Debug 构建变体的程序版本号为 null
 * 版本号会传入 Camera 如果为空则会崩溃
 */
public class RtcActivity extends AppCompatActivity {

    private static final String TAG = "RtcActivity";

    private static final int PERMISSION_REQ_ID = 22;

    private RtcManager manager;

    /**
     * 按设备Android版本获得视频通话功能需要的权限
     * @return 权限字符串数组
     */
    private String[] getRequiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
//                    Manifest.permission.READ_PHONE_STATE,
//                    Manifest.permission.BLUETOOTH_CONNECT
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

        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");

        manager = new RtcManager(userId);

        if (checkPermissions()) {
            bindView();
            initTrtcApplicationAndPresenter();
            mCloud.startLocalPreview(true, video);
            manager.startVideoCall();
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
            bindView();
            initTrtcApplicationAndPresenter();
            mCloud.startLocalPreview(true, video);
            manager.startVideoCall();
        } else {
            Toast.makeText(RtcActivity.this, "缺少视频通话必要的权限", Toast.LENGTH_SHORT).show();
            openAppPermissionSettings();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private TRTCCloud mCloud;
    private TXCloudVideoView video;
    private TXCloudVideoView camera;

    private void bindView() {
        video = findViewById(R.id.video);
        camera = findViewById(R.id.camera);
    }

    private void openAppPermissionSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void initTrtcApplicationAndPresenter() {
        mCloud = TRTCCloud.sharedInstance(getApplicationContext());
        mCloud.addListener(new TRTCCloudListener() {
            @Override
            public void onError(int errCode, String errMsg, Bundle extraInfo) {
                super.onError(errCode, errMsg, extraInfo);
                Toast.makeText(getApplicationContext(), "视频通话初始化出现错误（错误码：" + errCode + ")", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onEnterRoom(long result) {
                super.onEnterRoom(result);
                if (result > 0) {
                    Log.d(TAG, "initTrtcApplicationAndPresenter TRTCCloudListener: 房间进入成功");
                } else {
                    Log.d(TAG, "initTrtcApplicationAndPresenter TRTCCloudListener: 房间进入失败");
                }
            }

            @Override
            public void onExitRoom(int reason) {
                super.onExitRoom(reason);
                Log.d(TAG, "initTrtcApplicationAndPresenter TRTCCloudListener: 已退出房间");
            }

            @Override
            public void onUserVideoAvailable(String userId, boolean available) {
                super.onUserVideoAvailable(userId, available);
                Log.d(TAG, "initTrtcApplicationAndPresenter TRTCCloudListener: 对方视频流准备完成 开始推流");
                mCloud.startRemoteView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, video);
                mCloud.startLocalPreview(true, camera);
            }
        });
        manager.initCloud(mCloud);
    }

}

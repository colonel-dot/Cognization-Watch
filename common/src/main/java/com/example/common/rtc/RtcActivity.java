package com.example.common.rtc;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.common.R;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import java.util.Locale;


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
        String targetId =  intent.getStringExtra("targetId");
        boolean isElder = intent.getBooleanExtra("isElder", false);

        manager = new RtcManager(this, userId, targetId, isElder);

        if (checkPermissions()) {
            bindView();
            initTimer();
            initTrtcApplicationAndPresenter();
            manager.startLocalPreview(true, video);
            manager.startVideoCall();
            bindClickListener();
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
            initTimer();
            initTrtcApplicationAndPresenter();
            manager.startLocalPreview(true, video);
            manager.startVideoCall();
            bindClickListener();
        } else {
            Toast.makeText(RtcActivity.this, "缺少视频通话必要的权限", Toast.LENGTH_SHORT).show();
            openAppPermissionSettings();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.destroy();
        }
        handler.removeCallbacks(timerRunnable);
    }

    private TXCloudVideoView video;
    private TXCloudVideoView camera;

    private TextView time;
    private ImageView mute;
    private ImageView hangup;
    private ImageView flip;
    private LinearLayout control;
    private View rootLayout;
    private TRTCCloud cloud;
    private TRTCCloudListener listener;

    private boolean isMuted = false;

    private Handler handler = new Handler(Looper.getMainLooper());
    private long startTimeMillis;
    private Runnable timerRunnable;

    private void bindView() {
        video = findViewById(R.id.video);
        camera = findViewById(R.id.camera);

        time = findViewById(R.id.time);
        mute = findViewById(R.id.mute);
        hangup = findViewById(R.id.hangup);
        flip = findViewById(R.id.flip);
        control = findViewById(R.id.control);

        mute.setImageResource(R.drawable.mic);

        rootLayout = findViewById(R.id.rtc);
    }

    private void bindClickListener() {
        mute.setOnClickListener(v -> {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mute, "alpha", 1f, 0.5f);
            fadeOut.setDuration(75);

            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mute, "alpha", 0.5f, 1f);
            fadeIn.setDuration(75);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(fadeOut, fadeIn);
            animatorSet.start();

            isMuted = !isMuted;
            if (manager != null) {
                manager.muteLocalAudio(isMuted);
            }
            mute.setImageResource(isMuted ? R.drawable.nomic : R.drawable.mic);
        });
        hangup.setOnClickListener(v -> {
            if (manager != null) {
                manager.exitRoom();
            }
            finish();
        });
        flip.setOnClickListener(v -> {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(flip, "alpha", 1f, 0.5f);
            fadeOut.setDuration(75);

            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(flip, "alpha", 0.5f, 1f);
            fadeIn.setDuration(75);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(fadeOut, fadeIn);
            animatorSet.start();

            manager.switchCamera();
        });
        if (rootLayout != null) {
            rootLayout.setOnClickListener(v -> toggleControlVisibility());
        }
    }

    private void initTimer() {
        startTimeMillis = System.currentTimeMillis();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
                int totalSeconds = (int) (elapsedMillis / 1000);
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;

                String text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                if (time != null) time.setText(text);

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timerRunnable);
    }

    private void toggleControlVisibility() {
        if (control == null) return;

        if (control.getVisibility() == View.VISIBLE) {
            control.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction(() -> control.setVisibility(View.GONE))
                    .start();
        } else {
            control.setAlpha(0f);
            control.setVisibility(View.VISIBLE);

            control.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start();
        }
    }

    private void openAppPermissionSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void initTrtcApplicationAndPresenter() {
        cloud = TRTCCloud.sharedInstance(getApplicationContext());
        listener = new TRTCCloudListener() {
            @Override
            public void onError(int errCode, String errMsg, Bundle extraInfo) {
                super.onError(errCode, errMsg, extraInfo);
                Log.e(TAG, "TRTC onError - errCode: " + errCode + ", errMsg: " + errMsg);
                Toast.makeText(getApplicationContext(), "视频通话初始化出现错误（错误码：" + errCode + ")", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onEnterRoom(long result) {
                super.onEnterRoom(result);
                Log.d(TAG, "onEnterRoom - result: " + result);
                if (result > 0) {
                    Log.d(TAG, "initTrtcApplicationAndPresenter TRTCCloudListener: 房间进入成功");
                } else {
                    Log.d(TAG, "initTrtcApplicationAndPresenter TRTCCloudListener: 房间进入失败");
                    // result 为负数表示错误码
                    Toast.makeText(getApplicationContext(), "房间进入失败，错误码：" + result, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onExitRoom(int reason) {
                super.onExitRoom(reason);
                Log.d(TAG, "onExitRoom called with reason: " + reason);
                runOnUiThread(() -> {
                    // TODO
                    if (!isFinishing()) {
                        finish();
                        Log.d(TAG, "Activity finished from onExitRoom");
                    } else {
                        Log.d(TAG, "Activity already finishing, skip finish()");
                    }
                });
            }

            @Override
            public void onUserVideoAvailable(String userId, boolean available) {
                super.onUserVideoAvailable(userId, available);
                Log.d(TAG, "initTrtcApplicationAndPresenter TRTCCloudListener: 对方视频流准备完成 开始推流");
                cloud.startRemoteView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, video);
                cloud.startLocalPreview(true, camera);
            }
        };
        cloud.addListener(listener);
        manager.initCloud(cloud);
    }

}

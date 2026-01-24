package com.example.bridge.ui;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.model.LatLng;
import com.example.bridge.R;
import com.example.bridge.feature.location.GeofenceManager;
import com.example.bridge.feature.location.LocationFeature;
import com.example.bridge.model.location.GeofenceInfo;

public class DebugActivity extends AppCompatActivity implements LocationFeature.LostStateListener, GeofenceManager.OnTraceReadyListener {

    private GeofenceManager geofenceManager;
    private LocationClient mLocationClient;
    private static final String TAG = "DebugActivity";
    private static final String NOTIFICATION_CHANNEL_ID = "GeofenceChannel";

    long serviceId = 242705; // Cogwatch-Debug
    String entityName = "trace";

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                if (Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_FINE_LOCATION))) {
                    Log.d(TAG, "位置权限已获取，继续检查定位服务");
                    checkLocationServiceAndProceed();
                } else {
                    Log.e(TAG, "位置权限被拒绝，地理围栏功能不可用");
                    Toast.makeText(this, "地理围栏功能需要位置权限", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Activity onCreate，开始进行前置条件检查");
        checkPermissionsAndSettings();
    }

    private void checkPermissionsAndSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION});
        } else {
            requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private void checkLocationServiceAndProceed() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e(TAG, "系统定位服务未开启，提示用户开启");
            Toast.makeText(this, "请开启系统定位服务以使用地理围栏功能", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        } else {
            Log.d(TAG, "系统定位服务已开启，所有前置条件满足，开始启动业务逻辑");
            startGeofenceLogic();
        }
    }

    private void startGeofenceLogic() {
        LBSTraceClient.setAgreePrivacy(this, true);
        LocationClient.setAgreePrivacy(true);
        try {
            LBSTraceClient client = new LBSTraceClient(getApplicationContext());
            client.setInterval(5, 15);

            Trace trace = new Trace(serviceId, entityName, false);
            Notification notification = createForegroundNotification();
            trace.setNotification(notification);

            geofenceManager = new GeofenceManager(client, trace);
            geofenceManager.setLostStateListener(this);
            geofenceManager.setTraceReadyListener(this);

            geofenceManager.start();
            Log.d(TAG, "已调用 manager.start()，并设置了采集频率和前台服务。等待 onTraceReady 回调...");
        } catch (Exception e) {
            Log.e(TAG, "初始化LBSTraceClient或LocationClient失败", e);
        }
    }
    
    private Notification createForegroundNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "地理围栏服务", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("位置服务运行中")
                .setContentText("正在持续监控您的地理围栏状态")
                .setSmallIcon(R.mipmap.ic_launcher) 
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    @Override
    public void onTraceReady() {
        Log.i(TAG, "onTraceReady 回调被触发，现在开始获取当前位置以创建围栏");
        getCurrentLocationAndCreateFence();
    }

    private void getCurrentLocationAndCreateFence() {
        try {
            // 初始化定位客户端
            mLocationClient = new LocationClient(getApplicationContext());

            // 注册定位监听器
            mLocationClient.registerLocationListener(new MyLocationListener());

            // 配置定位参数
            LocationClientOption option = new LocationClientOption();
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            option.setCoorType("bd09ll"); // 返回百度经纬度坐标
            option.setOpenGnss(true); // GPS
            option.setScanSpan(0); // 单次定位
            option.setIsNeedAddress(true); // 地址信息
            mLocationClient.setLocOption(option);
            // 4. 开始定位
            mLocationClient.start();
            Log.d(TAG, "百度定位SDK已启动，请求单次定位");
        } catch (Exception e) {
            Log.e(TAG, "初始化百度定位SDK失败", e);
        }
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                Log.e(TAG, "定位失败：BDLocation 对象为空");
                return;
            }
            // 状态码61、161代表定位成功
            if (location.getLocType() == 61 || location.getLocType() == 161) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.i(TAG, "成功获取当前位置：纬度 = " + latitude + " 经度 = " + longitude);

                // 使用获取到的位置创建围栏
                LatLng center = new LatLng(latitude, longitude);
                GeofenceInfo geofenceInfo = GeofenceInfo.newCircularFence(1, "Home", entityName, center, 0.01f);
                geofenceManager.createCircularFence(geofenceInfo);

                // 单次定位成功后，停止定位以节省资源
                mLocationClient.stop();
                Log.d(TAG, "单次定位成功，已停止百度定位SDK");
            } else {
                Log.e(TAG, "定位失败，错误码: " + location.getLocType() + " 请参考百度定位SDK文档查找原因。");
            }
        }
    }

    @Override
    public void onLostStateChanged(boolean isLost) {
        runOnUiThread(() -> {
            if (isLost) {
                Log.e(TAG, "用户疑似走失！");
                Toast.makeText(this, "用户疑似走失！", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "用户已在围栏内，状态正常", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (geofenceManager != null) {
            Log.d(TAG, "Activity onDestroy，停止地理围栏服务");
            geofenceManager.stop();
        }
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
    }
}

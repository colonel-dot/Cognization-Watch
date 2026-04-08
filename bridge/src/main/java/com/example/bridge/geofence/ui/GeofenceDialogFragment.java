package com.example.bridge.geofence.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.bridge.R;
import com.example.bridge.geofence.feature.GeofenceManager;
import com.example.bridge.geofence.feature.GeofenceStatusManager;
import com.google.android.material.button.MaterialButton;

public class GeofenceDialogFragment extends DialogFragment {

    private MaterialButton btnConfirm;
    private SeekBar seekBar;
    private TextView tvValue;

    private GeofenceManager geofenceManager;

    public interface OnFenceCreatedListener {
        void onFenceCreated(String customId, double lat, double lng, float radius);
    }

    private OnFenceCreatedListener listener;

    public void setOnFenceCreatedListener(OnFenceCreatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog);
        geofenceManager = new GeofenceManager(requireContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            android.view.Window window = getDialog().getWindow();
            android.view.WindowManager.LayoutParams params = window.getAttributes();
            int widthInPx = (int) (320 * getResources().getDisplayMetrics().density);
            params.width = widthInPx;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_fence, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnConfirm = view.findViewById(R.id.fence_button);
        seekBar = view.findViewById(R.id.fence_seekbar);
        tvValue = view.findViewById(R.id.fence_value_text);

        // 初始化 SeekBar 显示
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int radius = Math.max(progress, 3000);
                tvValue.setText("当前范围: " + radius + "m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        int initialRadius = Math.max(seekBar.getProgress(), 3000);
        tvValue.setText("当前范围: " + initialRadius + "m");

        btnConfirm.setOnClickListener(v -> {
            int radius = Math.max(seekBar.getProgress(), 3000);
            try {
                createFence((float) radius);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void createFence(float radius) throws Exception {
        // 使用高德定位 SDK 获取当前设备位置
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setOnceLocation(true);

        AMapLocationClient locationClient = new AMapLocationClient(requireContext());
        locationClient.setLocationOption(option);
        locationClient.setLocationListener(location -> {
            locationClient.stopLocation();
            locationClient.onDestroy();

            if (location.getErrorCode() == 0) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                com.amap.api.location.DPoint currentPoint = new com.amap.api.location.DPoint(latitude, longitude);
                String customId = "fence_" + System.currentTimeMillis();

                GeofenceManager.Status status = geofenceManager.CreateLatLngGeofence(
                    requireContext(),
                    currentPoint,
                    radius,
                    customId
                );

                if (status == GeofenceManager.Status.SUCCESS) {
                    GeofenceStatusManager.saveFenceInfo(
                        requireContext(),
                        customId,
                        radius,
                        latitude,
                        longitude
                    );
                    GeofenceStatusManager.setFenceEnabled(requireContext(), true);

                    Toast.makeText(getContext(), "围栏创建成功", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onFenceCreated(customId, latitude, longitude, radius);
                    }
                    dismiss();
                } else if (status == GeofenceManager.Status.REPEATED_CREATION) {
                    Toast.makeText(getContext(), "围栏已存在，请勿重复创建", Toast.LENGTH_SHORT).show();
                } else if (status == GeofenceManager.Status.FAILURE) {
                    Toast.makeText(getContext(), "围栏创建失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "定位失败: " + location.getErrorInfo(), Toast.LENGTH_SHORT).show();
            }
        });
        locationClient.startLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        geofenceManager = null;
    }
}

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
            createFence((float) radius);
        });
    }

    private void createFence(float radius) {
        // TODO: 获取实际位置（目前使用固定坐标，或从地图/定位获取）
        // 这里参考 GeofenceManager.CreateLatLngGeofence 的用法
        // 需要 DPoint (高德 DPoint)，可从定位或地图中心获取
        // 临时使用默认坐标，后续应从地图/定位获取
        com.amap.api.location.DPoint defaultPoint = new com.amap.api.location.DPoint(34.261111, 108.942222);
        String customId = "fence_" + System.currentTimeMillis();

        GeofenceManager.Status status = geofenceManager.CreateLatLngGeofence(
            requireContext(),
            defaultPoint,
            radius,
            customId
        );

        if (status == GeofenceManager.Status.SUCCESS) {
            // 保存围栏信息到 GeofenceStatusManager
            GeofenceStatusManager.saveFenceInfo(
                requireContext(),
                customId,
                radius,
                defaultPoint.getLatitude(),
                defaultPoint.getLongitude()
            );
            GeofenceStatusManager.setFenceEnabled(requireContext(), true);

            Toast.makeText(getContext(), "围栏创建成功", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onFenceCreated(customId, defaultPoint.getLatitude(), defaultPoint.getLongitude(), radius);
            }
            dismiss();
        } else if (status == GeofenceManager.Status.REPEATED_CREATION) {
            Toast.makeText(getContext(), "围栏已存在，请勿重复创建", Toast.LENGTH_SHORT).show();
        } else if (status == GeofenceManager.Status.FAILURE) {
            Toast.makeText(getContext(), "围栏创建失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        geofenceManager = null;
    }
}

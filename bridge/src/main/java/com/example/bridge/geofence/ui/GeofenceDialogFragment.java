package com.example.bridge.geofence.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.example.bridge.R;
import com.example.bridge.geofence.feature.GeofenceStatusManager;
import com.google.android.material.button.MaterialButton;

public class GeofenceDialogFragment extends DialogFragment {

    private static final String TAG = "GeofenceDialogFragment";

    private MaterialButton btnConfirm;
    private SeekBar seekBar;
    private TextView tvValue;

    // 围栏参数准备好后通知 GeofenceFragment 发送给绑定设备
    public interface OnFenceCreatedListener {
        void onFenceCreated(double lat, double lng, float radius);
    }

    private OnFenceCreatedListener listener;

    public void setOnFenceCreatedListener(OnFenceCreatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            android.view.Window window = getDialog().getWindow();
            android.view.WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (320 * getResources().getDisplayMetrics().density);
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

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    try {
                        createFence(seekBar.getProgress() > 0 ? seekBar.getProgress() : 3000);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(getContext(), "需要定位权限才能创建围栏", Toast.LENGTH_SHORT).show();
                }
            });

    public AMapLocationClient locationClient = null;
    public AMapLocationClientOption option = null;

    private void createFence(float radius) throws Exception {
        Log.d(TAG, "come to createFence");

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setOnceLocation(true);
        option.setOnceLocationLatest(true);

        // 隐私合规检查
        AMapLocationClient.updatePrivacyShow(requireContext(), true, true);
        AMapLocationClient.updatePrivacyAgree(requireContext(), true);

        locationClient = new AMapLocationClient(requireContext());
        locationClient.setLocationOption(option);
        locationClient.setLocationListener(location -> {
            Log.d(TAG, "location callback, errorCode=" + location.getErrorCode());
            requireActivity().runOnUiThread(() -> {
                if (location.getErrorCode() == 0) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    Log.d(TAG, "获取位置成功: lat=" + latitude + ", lng=" + longitude + ", radius=" + radius);

                    GeofenceStatusManager.setFenceEnabled(requireContext(), true);
                    GeofenceStatusManager.saveFenceInfo(requireContext(), "", radius, latitude, longitude);

                    if (listener != null) {
                        listener.onFenceCreated(latitude, longitude, radius);
                    }
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "定位失败: " + location.getErrorInfo(), Toast.LENGTH_SHORT).show();
                }

                locationClient.stopLocation();
                locationClient.onDestroy();
            });
        });
        Log.d(TAG, "startLocation");
        locationClient.startLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        option = null;
    }
}
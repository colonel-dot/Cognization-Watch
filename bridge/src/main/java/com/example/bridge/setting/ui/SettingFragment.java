package com.example.bridge.setting.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bridge.R;
import com.example.bridge.geofence.ui.GeofenceDialogFragment;
import com.example.bridge.geofence.vm.GeoViewModel;
import com.example.bridge.setting.item.SettingItem;
import com.example.common.geofence.model.BarrierInfo;
import com.example.common.login.GuestStateHolder;
import com.example.common.login.remote.LoginStatusManager;
import com.example.common.bind_device.BindStatusManager;
import com.example.common.persistense.BusinessDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";

    private RecyclerView content;
    private LinearLayout signout;
    private SettingAdapter adapter;
    private GeoViewModel viewModel;
    private final ExecutorService logoutExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                Context context = getContext();
                if (context == null) {
                    return;
                }
                if (isGranted) {
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager == null || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Toast.makeText(context, "请先开启定位服务", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    } else {
                        showGeofenceDialog();
                    }
                } else {
                    Toast.makeText(context, "需要定位权限才能设置围栏", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bridge_fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindView(view);
        initViewModel();
        initRVAdapter();
        initListener();
        observeUiState();
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(GeoViewModel.class);
    }

    private void observeUiState() {
        viewModel.getBarrierUiState().observe(getViewLifecycleOwner(), state -> {
            if (viewModel.isBarrierPostSuccess(state)) {
                Log.d("SettingFragment", "围栏信息发送到远端成功");
                Toast.makeText(requireContext(), "围栏绑定成功", Toast.LENGTH_SHORT).show();
            } else if (viewModel.isBarrierError(state)) {
                String msg = viewModel.getBarrierErrorMsg(state);
                Log.e("SettingFragment", "围栏信息发送到远端失败: " + msg);
                Toast.makeText(requireContext(), "围栏绑定失败: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showGeofenceDialog() {
        if (!isAdded()) {
            return;
        }
        GeofenceDialogFragment dialog = new GeofenceDialogFragment();
        dialog.setOnFenceCreatedListener((lat, lng, radius) -> {
            Log.d("SettingFragment", "围栏参数获取成功: lat=" + lat + ", lng=" + lng + ", radius=" + radius);
            sendBarrierInfoToRemote(lat, lng, radius);
        });
        dialog.show(getChildFragmentManager(), "GeofenceDialog");
    }

    private void sendBarrierInfoToRemote(double lat, double lng, float radius) {
        if (GuestStateHolder.INSTANCE.isGuest()) {
            Log.d("SettingFragment", "游客模式，跳过围栏信息上传");
            return;
        }
        String eldername = LoginStatusManager.INSTANCE.getLoggedInUserId(requireContext());
        BarrierInfo barrierInfo = new BarrierInfo(eldername, lng, lat, radius);
        viewModel.postBarrierInfo(eldername, barrierInfo);
    }

    private void checkLocationPermissionAndShowDialog() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context, "请先开启定位服务", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            showGeofenceDialog();
        }
    }

    private void bindView(View view) {
        content = view.findViewById(R.id.content);
        signout = view.findViewById(R.id.signout);
    }

    private void initRVAdapter() {
        List<SettingItem> items = new ArrayList<>();
        items.add(new SettingItem(R.drawable.profiles, "设置绑定用户备注", 0, false));
        items.add(new SettingItem(R.drawable.map, "重设围栏", 1, false));

        adapter = new SettingAdapter(items, new SettingAdapter.OnSettingsClickListener() {
            @Override
            public void onItemClick(SettingItem item) {
                switch (item.getPosition()) {
                    case 0 -> new RemarkDialogFragment().show(getChildFragmentManager(), "RemarkDialog");
                    case 1 -> checkLocationPermissionAndShowDialog();
                }
            }

            @Override
            public void onSwitchChanged(SettingItem item, boolean isChecked) {
                switch (item.getPosition()) {
                    case 0, 1 -> { }
                }
            }
        });

        content.setLayoutManager(new LinearLayoutManager(requireContext()));
        content.setAdapter(adapter);
    }

    private void initListener() {
        signout.setOnClickListener(v -> showLogoutConfirmDialog());
    }

    private void showLogoutConfirmDialog() {
        if (!isAdded()) {
            return;
        } else {
            requireContext();
        }
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("确认退出")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("退出", (dia, which) -> {
                    if (isAdded()) {
                        performLogout();
                    }
                })
                .setNegativeButton("取消", null)
                .create();

        dialog.show();

        Drawable background = ContextCompat.getDrawable(requireContext(), R.drawable.bridge_background_rounded);
        if (background != null) {
            Drawable wrappedDrawable = DrawableCompat.wrap(background.mutate());
            DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(requireContext(), R.color.white_background));
            dialog.getWindow().setBackgroundDrawable(wrappedDrawable);
        }
    }

    private void performLogout() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        LoginStatusManager.INSTANCE.logout(appContext);
        BindStatusManager.INSTANCE.clearBindStatus(appContext);

        logoutExecutor.execute(() -> {
            try {
                BusinessDataManager.INSTANCE.clearAll(appContext);
            } catch (Exception e) {
                Log.e(TAG, "Failed to clear business data during logout", e);
            } finally {
                mainHandler.post(() -> {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();
                Intent intent = requireContext()
                        .getPackageManager()
                        .getLaunchIntentForPackage(requireContext().getPackageName());
                if (intent == null) {
                    Log.e(TAG, "Unable to resolve launcher activity after logout");
                    return;
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logoutExecutor.shutdown();
    }
}

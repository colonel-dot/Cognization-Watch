package com.example.bridge.setting.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bridge.R;
import com.example.bridge.geofence.ui.GeofenceDialogFragment;
import com.example.bridge.geofence.vm.GeoViewModel;
import com.example.bridge.setting.item.SettingItem;
import com.example.common.geofence.model.BarrierInfo;
import com.example.common.login.remote.LoginStatusManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    // Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView content;
    private LinearLayout signout;
    private SettingAdapter adapter;
    private GeoViewModel viewModel;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Toast.makeText(requireContext(), "请先开启定位服务", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    } else {
                        showGeofenceDialog();
                    }
                } else {
                    Toast.makeText(requireContext(), "需要定位权限才能设置围栏", Toast.LENGTH_SHORT).show();
                }
            });

    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
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
            if (state == null) return;

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
        GeofenceDialogFragment dialog = new GeofenceDialogFragment();
        dialog.setOnFenceCreatedListener((lat, lng, radius) -> {
            Log.d("SettingFragment", "围栏参数获取成功: lat=" + lat + ", lng=" + lng + ", radius=" + radius);
            sendBarrierInfoToRemote(lat, lng, radius);
        });
        dialog.show(getChildFragmentManager(), "GeofenceDialog");
    }

    private void sendBarrierInfoToRemote(double lat, double lng, float radius) {
        String eldername = LoginStatusManager.INSTANCE.getLoggedInUserId(requireContext());
        BarrierInfo barrierInfo = new BarrierInfo(eldername, lng, lat, (double) radius);
        viewModel.postBarrierInfo(eldername, barrierInfo);
    }

    private void checkLocationPermissionAndShowDialog() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(requireContext(), "请先开启定位服务", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
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
                    case 0 -> { }
                    case 1 -> { }
                }
            }
        });

        content.setLayoutManager(new LinearLayoutManager(requireContext()));
        content.setAdapter(adapter);
    }

    private void initListener() {
        signout.setOnClickListener(v -> {
            // TODO: sign out
        });
    }
}
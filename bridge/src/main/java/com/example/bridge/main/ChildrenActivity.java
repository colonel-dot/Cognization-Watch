package com.example.bridge.main;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.bridge.R;
import com.example.bridge.dashboard.ui.DashboardFragment;
import com.example.bridge.geofence.ui.GeofenceFragment;
import com.example.bridge.record.ui.RecordFragment;
import com.example.bridge.setting.ui.SettingFragment;
import com.example.common.login.LoginPopupProvider;
import com.example.common.router.RouterPaths;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChildrenActivity extends AppCompatActivity {

    private static final String TAG = "ChildrenActivity";

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_children);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigation = findViewById(R.id.navigation);

        initBottomNavigation();

        ARouter.getInstance()
                .build(RouterPaths.POPUP_LOGIN)
                .greenChannel()
                .navigation(this, new NavigationCallback() {
                    @Override
                    public void onFound(Postcard postcard) {
                        Log.d(TAG, "路由找到: " + postcard.getPath());
                        // 获取 IProvider 并调用 showPopup()
                        IProvider provider = (IProvider) ARouter.getInstance().build(RouterPaths.POPUP_LOGIN).navigation();
                        provider.init(ChildrenActivity.this);
                        if (provider instanceof LoginPopupProvider) {
                            ((LoginPopupProvider) provider).showPopup();
                        }
                    }

                    @Override
                    public void onLost(Postcard postcard) {
                        Log.e(TAG, "路由未找到: " + postcard.getPath());
                        Toast.makeText(ChildrenActivity.this, "路由未找到: " + postcard.getPath(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onInterrupt(Postcard postcard) {
                        Log.w(TAG, "路由被拦截: " + postcard.getPath());
                    }

                    @Override
                    public void onArrival(Postcard postcard) {
                        Log.d(TAG, "路由到达: " + postcard.getPath());
                    }
                });
    }

    private void initBottomNavigation() {
        Log.d(TAG, "initBottomNavigation");
        bottomNavigation.setOnItemSelectedListener(item -> {
            switchFragment(item.getItemId());
            return true;
        });

        bottomNavigation.setSelectedItemId(R.id.dashboard);
    }

    /** 切换到底部导航对应的 Fragment，并更新导航栏选中状态 */
    public void switchToGeofenceFragment() {
        bottomNavigation.setSelectedItemId(R.id.geofence);
    }

    /** 根据 itemId 切换 Fragment */
    private void switchFragment(int itemId) {
        Log.d(TAG, "Bottom nav item selected: " + itemId);

        String targetTag = null;
        Fragment selectedFragment = null;

        if (itemId == R.id.dashboard) {
            targetTag = "DASHBOARD";
            selectedFragment = new DashboardFragment();
            Log.d(TAG, "Dashboard selected");
        } else if (itemId == R.id.data) {
            targetTag = "DATA";
            selectedFragment = new RecordFragment();
            Log.d(TAG, "Data selected");
        } else if (itemId == R.id.geofence) {
            targetTag = "GEOFENCE";
            selectedFragment = new GeofenceFragment();
            Log.d(TAG, "Geofence selected");
        } else if (itemId == R.id.settings) {
            targetTag = "SETTINGS";
            selectedFragment = new SettingFragment();
            Log.d(TAG, "Profiles selected");
        }

        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        Log.d(TAG, "Current fragment: " + currentFragment + ", tag: " + (currentFragment != null ? currentFragment.getTag() : "null"));

        if (currentFragment != null && currentFragment.getTag() != null
                && currentFragment.getTag().equals(targetTag)) {
            Log.d(TAG, "Already on target fragment, skipping");
            return;
        }

        if (selectedFragment != null) {
            Log.d(TAG, "Committing fragment transaction for tag: " + targetTag);
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment, targetTag)
                        .commit();
                Log.d(TAG, "Fragment transaction committed");
            } catch (Exception e) {
                Log.e(TAG, "Error committing fragment transaction", e);
            }
        } else {
            Log.e(TAG, "selectedFragment is null!");
        }
    }
}
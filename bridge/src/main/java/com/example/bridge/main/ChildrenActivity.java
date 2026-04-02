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
import com.example.bridge.profiles.ProfilesFragment;
import com.example.bridge.record.ui.RecordFragment;
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
                        // 获取 IProvider 并调用 showPopup()
                        IProvider provider = (IProvider) ARouter.getInstance().build(RouterPaths.POPUP_LOGIN).navigation();
                        if (provider instanceof LoginPopupProvider) {
                            ((LoginPopupProvider) provider).showPopup();
                        }
                    }
                });
        // 直接获取并调用（备用方案，确保弹窗能弹出）
        try {
            LoginPopupProvider provider = (LoginPopupProvider) ARouter.getInstance().build(RouterPaths.POPUP_LOGIN).navigation();
            if (provider != null) {
                // 确保 init 被调用
                provider.init(this);
                provider.showPopup();
                Log.d(TAG, "弹窗已显示");
            }
        } catch (Exception e) {
            Log.e(TAG, "获取 IProvider 失败", e);
        }
    }

    private void initBottomNavigation() {
        android.util.Log.d("ChildrenActivity", "initBottomNavigation");
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
        android.util.Log.d("ChildrenActivity", "Bottom nav item selected: " + itemId);

        String targetTag = null;
        Fragment selectedFragment = null;

        if (itemId == R.id.dashboard) {
            targetTag = "DASHBOARD";
            selectedFragment = DashboardFragment.newInstance("", "");
            android.util.Log.d("ChildrenActivity", "Dashboard selected");
        } else if (itemId == R.id.data) {
            targetTag = "DATA";
            selectedFragment = RecordFragment.newInstance("", "");
            android.util.Log.d("ChildrenActivity", "Data selected");
        } else if (itemId == R.id.geofence) {
            targetTag = "GEOFENCE";
            selectedFragment = GeofenceFragment.newInstance("", "");
            android.util.Log.d("ChildrenActivity", "Geofence selected");
        } else if (itemId == R.id.profiles) {
            targetTag = "PROFILES";
            selectedFragment = ProfilesFragment.newInstance("", "");
            android.util.Log.d("ChildrenActivity", "Profiles selected");
        }

        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        android.util.Log.d("ChildrenActivity", "Current fragment: " + currentFragment + ", tag: " + (currentFragment != null ? currentFragment.getTag() : "null"));

        if (currentFragment != null && currentFragment.getTag() != null
                && currentFragment.getTag().equals(targetTag)) {
            android.util.Log.d("ChildrenActivity", "Already on target fragment, skipping");
            return;
        }

        if (selectedFragment != null) {
            android.util.Log.d("ChildrenActivity", "Committing fragment transaction for tag: " + targetTag);
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment, targetTag)
                        .commit();
                android.util.Log.d("ChildrenActivity", "Fragment transaction committed");
            } catch (Exception e) {
                android.util.Log.e("ChildrenActivity", "Error committing fragment transaction", e);
                e.printStackTrace();
            }
        } else {
            android.util.Log.e("ChildrenActivity", "selectedFragment is null!");
        }
    }
}
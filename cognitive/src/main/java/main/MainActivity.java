package main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.cognitive.R;
import com.example.cognitive.main.HomeFragment;
import com.example.cognitive.main.MainViewModel;
import com.example.common.login.LoginPopupProvider;
import com.example.common.router.RouterPaths;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import mine.ui.RecordFragment;
import settings.SettingsFragment;
import sports.data.StepForegroundService;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private BottomNavigationView bottomNavigation;
    private Fragment currentFragment;
    private MainViewModel mainViewModel;
    private ActivityResultLauncher<String[]> multiplePermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigation = findViewById(R.id.navigation);

        // 初始化权限请求 Launcher
        multiplePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    Log.d(TAG, "权限请求结果: " + permissions);
                    boolean isActivityRecognitionGranted = true;
                    // 检查 ACTIVITY_RECOGNITION 权限是否被授予
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Boolean activityRecognitionResult = permissions.get(Manifest.permission.ACTIVITY_RECOGNITION);
                        if (activityRecognitionResult != null && !activityRecognitionResult) {
                            isActivityRecognitionGranted = false;
                        }
                    }

                    if (isActivityRecognitionGranted) {
                        Log.d(TAG, "必要权限已获得，准备启动服务");
                        startStepService();
                    } else {
                        Toast.makeText(this, "需要活动识别权限才能计步", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        checkAndRequestPermissions();

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initBottomNavigation();

        ARouter.getInstance()
                .build(RouterPaths.POPUP_LOGIN)
                .navigation(this, new NavigationCallback() {
                    @Override
                    public void onFound(Postcard postcard) {
                        Log.d(TAG, "路由找到: " + postcard.getPath());
                    }

                    @Override
                    public void onLost(Postcard postcard) {
                        Log.e(TAG, "路由未找到: " + postcard.getPath());
                        Toast.makeText(MainActivity.this, "路由未找到: " + postcard.getPath(), Toast.LENGTH_SHORT).show();
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
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment selectFragment = null;
            Log.d(TAG, "bottomNavigation.setOnItemSelectedListener");
            if (item.getItemId() == R.id.home) {
                selectFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.history) {
                selectFragment = RecordFragment.newInstance("", "");
            } else if (item.getItemId() == R.id.settings) {
                selectFragment = SettingsFragment.newInstance("", "");
            }
            if (selectFragment != null) {
                switchFragment(selectFragment, false);
            }
            return true;
        });
        switchFragment(new HomeFragment(), false);
    }

    public void switchFragment(Fragment fragment, boolean addToBackStack) {
        // 清除回退栈，防止 Fragment 重叠
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (currentFragment == null) {
            transaction.add(R.id.fragment_container, fragment);
        } else {
            if (currentFragment == fragment) return;

            transaction.hide(currentFragment);

            if (fragment.isAdded()) {
                transaction.show(fragment);
            } else {
                transaction.add(R.id.fragment_container, fragment);
            }
        }

        transaction.commit();
        currentFragment = fragment;
    }

    private void checkAndRequestPermissions() {
        Log.d(TAG, "checkAndRequestPermissions");
        java.util.List<String> permissionsToRequest = new java.util.ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            // 使用 ActivityResultLauncher 请求权限
            multiplePermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            startStepService();
        }
    }


    private void startStepService() {
        Log.d(TAG, "startStepService");
        Intent intent = new Intent(this, StepForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
}
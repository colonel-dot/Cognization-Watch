package main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.cognitive.R;
import com.example.cognitive.main.HomeFragment;
import com.example.cognitive.main.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import mine.ui.RecordFragment;
import sports.data.StepForegroundService;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private Fragment currentFragment;
    private MainViewModel mainViewModel;
    private static final int REQ_NOTIFY = 100;

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

        checkAndRequestNotificationPermission();

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initBottomNavigation();
    }

    private void initBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                currentFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.history) {
                currentFragment = RecordFragment.newInstance("", "");
            } else if (item.getItemId() == R.id.settings) {
                // selectedFragment = SettingsFragment.newInstance("", "");
                return true;
            }
            if (currentFragment != null) {
                switchFragment(currentFragment, false);
            }
            return true;
        });
        switchFragment(new HomeFragment(), false);
    }

    public void switchFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (currentFragment == null) {
            transaction.add(R.id.fragment_container, fragment).commit();
            currentFragment = fragment;
            return;
        } else {
            if (currentFragment == fragment) {
                return;
            }
        }

        if (fragment.isAdded()) {
            transaction.hide(currentFragment).show(fragment).commit();
        } else {
            transaction.hide(currentFragment).add(R.id.fragment_container, fragment).commit();
        }

     /* if (addToBackStack) {
           transaction.addToBackStack(null);
        } */

        currentFragment = fragment;
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFY);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepService();
            } else {
                Toast.makeText(this, "用户权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startStepService() {
        Intent intent = new Intent(this, StepForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
}
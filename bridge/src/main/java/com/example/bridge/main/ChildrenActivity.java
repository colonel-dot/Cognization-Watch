package com.example.bridge.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.bridge.R;
import com.example.bridge.dashboard.ui.DashboardFragment;
import com.example.bridge.geofence.ui.GeofenceFragment;
import com.example.bridge.profiles.ProfilesFragment;
import com.example.bridge.record.ui.RecordFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChildrenActivity extends AppCompatActivity {

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
    }

    private void initBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {

            String targetTag = null;
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.dashboard) {
                targetTag = "DASHBOARD";
                selectedFragment = DashboardFragment.newInstance("", "");
            } else if (item.getItemId() == R.id.data) {
                targetTag = "DATA";
                selectedFragment = RecordFragment.newInstance("", "");
            } else if (item.getItemId() == R.id.geofence) {
                targetTag = "GEOFENCE";
                selectedFragment = GeofenceFragment.newInstance("", "");
            } else if (item.getItemId() == R.id.profiles) {
                targetTag = "PROFILES";
                selectedFragment = ProfilesFragment.newInstance("", "");
            }

            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);

            if (currentFragment != null && currentFragment.getTag() != null
                    && currentFragment.getTag().equals(targetTag)) {
                return false;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment, targetTag)
                        .commit();
            }

            return true;
        });

        bottomNavigation.setSelectedItemId(R.id.dashboard);
    }
}
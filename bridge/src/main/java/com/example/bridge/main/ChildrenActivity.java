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
        android.util.Log.d("ChildrenActivity", "initBottomNavigation");
        bottomNavigation.setOnItemSelectedListener(item -> {
            android.util.Log.d("ChildrenActivity", "Bottom nav item selected: " + item.getItemId());

            String targetTag = null;
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.dashboard) {
                targetTag = "DASHBOARD";
                selectedFragment = DashboardFragment.newInstance("", "");
                android.util.Log.d("ChildrenActivity", "Dashboard selected");
            } else if (item.getItemId() == R.id.data) {
                targetTag = "DATA";
                selectedFragment = RecordFragment.newInstance("", "");
                android.util.Log.d("ChildrenActivity", "Data selected");
            } else if (item.getItemId() == R.id.geofence) {
                targetTag = "GEOFENCE";
                selectedFragment = GeofenceFragment.newInstance("", "");
                android.util.Log.d("ChildrenActivity", "Geofence selected");
            } else if (item.getItemId() == R.id.profiles) {
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
                return false;
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

            return true;
        });

        bottomNavigation.setSelectedItemId(R.id.dashboard);
    }
}
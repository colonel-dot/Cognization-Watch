package com.example.cogwatch.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.cognitive.main.HomeFragment;
import com.example.cogwatch.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigation = findViewById(R.id.navigation);

        // ViewGroup menuView = (ViewGroup) bottomNavigation.getChildAt(0);
        // for (int i = 0; i < menuView.getChildCount(); i++) {
        //     View item = menuView.getChildAt(i);
        //     item.setOnLongClickListener(v -> true);
        // }

        initBottomNavigation();
    }

    private void initBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.home) {
                selectedFragment = HomeFragment.newInstance("", "");
            } else if (item.getItemId() == R.id.history) {
                // selectedFragment = HistoryFragment.newInstance("", "");
                // For now, do nothing
                return true;
            } else if (item.getItemId() == R.id.settings) {
                // selectedFragment = SettingsFragment.newInstance("", "");
                // For now, do nothing
                return true;
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
        // Set home as default selected
        bottomNavigation.setSelectedItemId(R.id.home);
    }
}
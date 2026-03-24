package main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.cognitive.R;
import com.example.cognitive.main.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private Fragment currentFragment;

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

        initBottomNavigation();
    }

    private void initBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                currentFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.history) {
                // selectedFragment = HistoryFragment.newInstance("", "");
                return true;
            } else if (item.getItemId() == R.id.settings) {
                // selectedFragment = SettingsFragment.newInstance("", "");
                return true;
            }
            if (currentFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, currentFragment)
                        .commit();
            }
            return true;
        });
        // Set home as default selected
        bottomNavigation.setSelectedItemId(R.id.home);
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
}
package setting.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import com.example.cognitive.R;

import java.util.ArrayList;
import java.util.List;

import risk.work.RiskConfigManager;
import schulte.data.SchulteEvaluatorType;
import setting.item.SettingItem;
import user.UserManager;
import com.example.common.login.remote.LoginStatusManager;
import com.example.common.bind_device.BindStatusManager;
import com.example.common.persistense.BusinessDataManager;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";

    public SettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    private RecyclerView content;
    private LinearLayout signout;

    private SettingAdapter adapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindView(view);
        initRVAdapter();
        initListener();
    }

    private void bindView(View view) {
        content = view.findViewById(R.id.content);
        signout = view.findViewById(R.id.signout);
    }

    private void initRVAdapter() {
        List<SettingItem> items = new ArrayList<>();
        SettingItem schulteItem = new SettingItem(R.drawable.game, "舒尔特方格 5×5模式", 0, true);
        schulteItem.setChecked(
            new RiskConfigManager(requireContext()).getSchulteEvaluatorType() == SchulteEvaluatorType.GRID_5
        );
        items.add(schulteItem);

        adapter = new SettingAdapter(items, new SettingAdapter.OnSettingsClickListener() {
            @Override
            public void onItemClick(SettingItem item) {
                switch (item.getPosition()) {
                    case 0:
                        break;
                }
            }

            @Override
            public void onSwitchChanged(SettingItem item, boolean isChecked) {
                switch (item.getPosition()) {
                    case 0:
                        SchulteEvaluatorType type = isChecked
                            ? SchulteEvaluatorType.GRID_5
                            : SchulteEvaluatorType.GRID_4;
                        new RiskConfigManager(requireContext())
                            .setSchulteEvaluatorType(type);
                        break;
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

        Drawable background = ContextCompat.getDrawable(requireContext(), R.drawable.background_rounded);
        if (background != null) {
            Drawable wrappedDrawable = DrawableCompat.wrap(background.mutate());
            DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(requireContext(), R.color.white_background));
            dialog.getWindow().setBackgroundDrawable(wrappedDrawable);
        }
    }

    private void performLogout() {
        LoginStatusManager.INSTANCE.logout(requireContext());
        BindStatusManager.INSTANCE.clearBindStatus(requireContext());
        UserManager.INSTANCE.clear();
        BusinessDataManager.INSTANCE.clearAll(requireContext());
        Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("com.example.cogwatch", "com.example.cogwatch.login.ui.LoginActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
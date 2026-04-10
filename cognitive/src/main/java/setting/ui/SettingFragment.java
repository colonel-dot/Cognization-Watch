package setting.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";

    // Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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

        SettingAdapter adapter = new SettingAdapter(items, new SettingAdapter.OnSettingsClickListener() {
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
        if (!isAdded() || requireContext() == null) {
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("确认退出")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("退出", (dialog, which) -> {
                    if (isAdded()) {
                        performLogout();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void performLogout() {
        LoginStatusManager.INSTANCE.logout(requireContext());
        BindStatusManager.INSTANCE.clearBindStatus(requireContext());
        UserManager.INSTANCE.clear();
        Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("com.example.cogwatch", "com.example.cogwatch.login.ui.LoginActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
package com.example.bridge.dashboard.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bridge.R;
import com.example.bridge.dashboard.data.DataInitializer;
import com.example.bridge.dashboard.item.DashboardAlertItem;
import com.example.bridge.dashboard.item.DashboardCollectionItem;
import com.example.bridge.dashboard.item.DashboardRiskItem;
import com.example.bridge.dashboard.item.DashboardRtcItem;
import com.example.cogwatch_ui.children.record.vm.RecordViewModel;
import com.example.common.rtc.RtcActivity;

import java.time.LocalDate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.common.persistense.AppDatabase;
import com.example.common.persistense.behavior.BehaviorRepository;
import com.example.common.persistense.behavior.DailyBehaviorDao;
import com.example.common.persistense.behavior.DailyBehaviorEntity;
import com.example.common.persistense.risk.RiskRepository;
import com.example.common.persistense.risk.DailyRiskDao;
import com.example.common.persistense.risk.DailyRiskEntity;
import com.example.bridge.util.ItemSpacingDecoration;

public class DashboardFragment extends Fragment {

    private String param1;
    private String param2;

    private RecyclerView recyclerView;
    private DashboardRVAdapter adapter;
    private RecordViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            param1 = getArguments().getString(ARG_PARAM1);
            param2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(
        @NonNull View view,
        @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.content);

        viewModel = new ViewModelProvider(this).get(RecordViewModel.class);

        adapter = new DashboardRVAdapter();
        adapter.list = new java.util.ArrayList<>();

        // 初始化占位数据
        adapter.list.add(new DashboardRtcItem("奥雷里亚诺", "在线"));
        adapter.list.add(new DashboardRiskItem(0.0, 0.0));
        adapter.list.add(new DashboardCollectionItem(0, 0.0));
        adapter.list.add(new DashboardAlertItem("长辈离开守护范围"));

        adapter.setOnRtcClickListener(position -> {
            // TODO RTC - 需要获取实际用户ID
            // 临时使用占位符，确保键名正确
            Intent intent = new Intent(getContext(), RtcActivity.class);
            intent.putExtra("userId", "test_child_user");
            intent.putExtra("targetId", "test_elder_user");
            intent.putExtra("isElder", false);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        );
        recyclerView.setAdapter(adapter);

        ItemSpacingDecoration decoration =
        new ItemSpacingDecoration(requireContext(), 20, false);
        recyclerView.addItemDecoration(decoration);

        // Initialize test data in database
        DataInitializer.INSTANCE.initializeTestData(requireContext());

        loadDashboardData();
    }

    private void loadDashboardData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Context context = getContext();
                if (context == null) return;

                AppDatabase database = AppDatabase.Companion.getDatabase(context);
                DailyRiskDao riskDao = database.dailyRiskDao();
                DailyBehaviorDao behaviorDao = database.dailyBehaviorDao();

                LocalDate today = LocalDate.now();
                LocalDate yesterday = today.minusDays(1);

                DailyRiskEntity todayRiskEntity = RiskRepository.INSTANCE.getByDateBlocking(riskDao, today);
                DailyRiskEntity yesterdayRiskEntity = RiskRepository.INSTANCE.getByDateBlocking(riskDao, yesterday);
                DailyBehaviorEntity todayBehaviorEntity = BehaviorRepository.INSTANCE.getByDateBlocking(behaviorDao, today);

                double todayRiskScore = todayRiskEntity != null ? todayRiskEntity.getRiskScore() : 0.0;
                double yesterdayRiskScore = yesterdayRiskEntity != null ? yesterdayRiskEntity.getRiskScore() : 0.0;

                final int todaySteps = (todayBehaviorEntity != null && todayBehaviorEntity.getSteps() != null)
                        ? todayBehaviorEntity.getSteps() : 0;
                final double todaySleepHours = (todayBehaviorEntity != null && todayBehaviorEntity.getSleepMinute() != null)
                        ? todayBehaviorEntity.getSleepMinute() / 60.0 : 0.0;

                requireActivity().runOnUiThread(() -> {
                    if (getView() == null) return;

                    if (adapter.list.size() > 1) {
                        adapter.list.set(1, new DashboardRiskItem(todayRiskScore, yesterdayRiskScore));
                    }
                    if (adapter.list.size() > 2) {
                        adapter.list.set(2, new DashboardCollectionItem(todaySteps, todaySleepHours));
                    }
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executor.shutdown();
    }

    public static DashboardFragment newInstance(String param1, String param2) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
}
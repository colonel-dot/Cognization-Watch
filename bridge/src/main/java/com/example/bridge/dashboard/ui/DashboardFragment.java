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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bridge.R;
import com.example.bridge.dashboard.item.DashboardAlertItem;
import com.example.bridge.dashboard.item.DashboardCollectionItem;
import com.example.bridge.dashboard.item.DashboardRiskItem;
import com.example.bridge.dashboard.item.DashboardRtcItem;
import com.example.cogwatch_ui.children.record.vm.RecordViewModel;
import com.example.bridge.main.ChildrenActivity;
import com.example.common.persistense.geofence.GeofencePersistenceProxy;
import com.example.common.rtc.RtcActivity;

import java.time.LocalDate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.widget.Toast;
import android.util.Log;

import com.example.common.login.remote.LoginStatusManager;
import com.example.common.bind_device.BindStatusManager;

import com.example.common.persistense.AppDatabase;
import com.example.common.persistense.behavior.BehaviorRepository;
import com.example.common.persistense.behavior.DailyBehaviorDao;
import com.example.common.persistense.behavior.DailyBehaviorEntity;
import com.example.common.persistense.geofence.GeofenceItem;
import com.example.common.persistense.geofence.GeofenceItemDao;
import com.example.common.persistense.risk.RiskRepository;
import com.example.common.persistense.risk.DailyRiskDao;
import com.example.common.persistense.risk.DailyRiskEntity;
import com.example.common.util.ItemSpacingDecoration;

public class DashboardFragment extends Fragment {

    private String param1;
    private String param2;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
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
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        viewModel = new ViewModelProvider(this).get(RecordViewModel.class);

        adapter = new DashboardRVAdapter();
        adapter.list = new java.util.ArrayList<>();

        // 初始化占位数据
        adapter.list.add(new DashboardRtcItem("没有数据源", "未知"));
        adapter.list.add(new DashboardRiskItem(0.0, 0.0));
        adapter.list.add(new DashboardCollectionItem(0, 0.0));
        adapter.list.add(new DashboardAlertItem("没有数据源"));

        adapter.setOnRtcClickListener(position -> {
            // 获取实际用户ID
            String userId = LoginStatusManager.INSTANCE.getLoggedInUserId(requireContext());
            // 获取对方ID
            String targetId = BindStatusManager.INSTANCE.getBindStatus().getSecond();

            if (userId == null || userId.isEmpty() || targetId == null || targetId.isEmpty()) {
                Log.e("DashboardFragment", "用户ID或对方ID为空，无法发起RTC通话");
                Toast.makeText(getContext(), "请先登录并绑定设备", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("DashboardFragment", "发起RTC通话: userId=" + userId + ", targetId=" + targetId);
            Intent intent = new Intent(getContext(), RtcActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("targetId", targetId);
            intent.putExtra("isElder", false);
            startActivity(intent);
        });

        adapter.setOnAlertClickListener(position -> {
            if (getActivity() instanceof ChildrenActivity) {
                ((ChildrenActivity) getActivity()).switchToGeofenceFragment();
            }
        });

        recyclerView.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        );
        recyclerView.setAdapter(adapter);

        ItemSpacingDecoration decoration =
        new ItemSpacingDecoration(requireContext(), 20, false);
        recyclerView.addItemDecoration(decoration);

        swipeRefresh.setOnRefreshListener(() -> {
            loadDashboardDataWithRefreshComplete();
        });

        try {
            loadDashboardData();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 加载数据并在下拉刷新完成后结束刷新状态 */
    private void loadDashboardDataWithRefreshComplete() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // 获取数据
                DashboardData data = loadDashboardData();

                requireActivity().runOnUiThread(() -> {
                    if (getView() == null) return;

                    updateDashboardUI(data);
                    swipeRefresh.setRefreshing(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> swipeRefresh.setRefreshing(false));
            }
        });
        executor.shutdown();
    }

    /** 后台线程中加载所有 Dashboard 数据 */
    private DashboardData loadDashboardData() throws Exception {
        Context context = getContext();
        if (context == null) throw new Exception("context is null");

        AppDatabase database = AppDatabase.Companion.getDatabase(context);
        DailyRiskDao riskDao = database.dailyRiskDao();
        DailyBehaviorDao behaviorDao = database.dailyBehaviorDao();
        GeofenceItemDao geofenceDao = database.geofenceItemDao();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        DailyRiskEntity todayRiskEntity = RiskRepository.INSTANCE.getByDateBlocking(riskDao, today);
        DailyRiskEntity yesterdayRiskEntity = RiskRepository.INSTANCE.getByDateBlocking(riskDao, yesterday);
        DailyBehaviorEntity todayBehaviorEntity = BehaviorRepository.INSTANCE.getByDateBlocking(behaviorDao, today);

        double todayRiskScore = todayRiskEntity != null ? todayRiskEntity.getRiskScore() : 0.0;
        double yesterdayRiskScore = yesterdayRiskEntity != null ? yesterdayRiskEntity.getRiskScore() : 0.0;

        int todaySteps = (todayBehaviorEntity != null && todayBehaviorEntity.getSteps() != null)
                ? todayBehaviorEntity.getSteps() : 0;
        double todaySleepHours = (todayBehaviorEntity != null && todayBehaviorEntity.getSleepMinute() != null)
                ? todayBehaviorEntity.getSleepMinute() / 60.0 : 0.0;

        // 获取最新的红色预警 GeofenceItem（定位失败状态）
        String alertTip;
        java.util.List<GeofenceItem> redAlertItems = GeofencePersistenceProxy.getByStatusSync(
                geofenceDao,
                GeofenceItem.STATUS_LOCAL
        );
        if (redAlertItems != null && !redAlertItems.isEmpty()) {
            GeofenceItem latestAlert = redAlertItems.get(0);
            long timestampMillis = latestAlert.getTimestamp() * 60L * 1000L;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault());
            String timeStr = sdf.format(new java.util.Date(timestampMillis));
            alertTip = "定位失败: " + timeStr;
        } else {
            alertTip = "没有数据源";
        }

        return new DashboardData(todayRiskScore, yesterdayRiskScore, todaySteps, todaySleepHours, alertTip);
    }

    /** 更新 Dashboard UI 数据 */
    private void updateDashboardUI(DashboardData data) {
        if (adapter.list.size() > 1) {
            adapter.list.set(1, new DashboardRiskItem(data.todayRiskScore, data.yesterdayRiskScore));
        }
        if (adapter.list.size() > 2) {
            adapter.list.set(2, new DashboardCollectionItem(data.todaySteps, data.todaySleepHours));
        }
        if (adapter.list.size() > 3) {
            adapter.list.set(3, new DashboardAlertItem(data.alertTip));
        }
        adapter.notifyDataSetChanged();
    }

    /** Dashboard 数据封装类 */
    private static class DashboardData {
        double todayRiskScore;
        double yesterdayRiskScore;
        int todaySteps;
        double todaySleepHours;
        String alertTip;

        DashboardData(double todayRiskScore, double yesterdayRiskScore, int todaySteps,
                      double todaySleepHours, String alertTip) {
            this.todayRiskScore = todayRiskScore;
            this.yesterdayRiskScore = yesterdayRiskScore;
            this.todaySteps = todaySteps;
            this.todaySleepHours = todaySleepHours;
            this.alertTip = alertTip;
        }
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
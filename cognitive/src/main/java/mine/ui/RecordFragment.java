package mine.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cognitive.R;
import com.example.common.persistense.AppDatabase;
import com.example.common.persistense.risk.DailyRiskEntity;
import com.example.common.persistense.risk.RiskRepository;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mine.test.DataInitializer;
import util.ItemSpacingDecoration;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RecordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordFragment newInstance(String param1, String param2) {
        RecordFragment fragment = new RecordFragment();
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
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    private TextView week;
    private TextView half;
    private LineChart lineChart;
    private RecyclerView record;
    private SwipeRefreshLayout swipeRefresh;
    private RecordRVAdapter adapter;
    private List<DailyRiskEntity> riskDataList = new ArrayList<>();
    private int selectedDays = 15;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        android.util.Log.d("RecordFragment", "onViewCreated");

        try {
            bindView(view);

            bindClickListener();

            initLineChart();

            initRecyclerView();
        } catch (Exception e) {
            android.util.Log.e("RecordFragment", "Error in onViewCreated", e);
            e.printStackTrace();
            throw e; // rethrow
        }
    }

    private void bindView(View view) {
        week = view.findViewById(R.id.week);
        half = view.findViewById(R.id.half);
        record = view.findViewById(R.id.record);
        lineChart = view.findViewById(R.id.lineChart);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
    }

    private void bindClickListener() {
        week.setOnClickListener(v -> {
            selectedDays = 7;
            updateButtonAppearance();
            loadRiskDataFromDatabase();
        });
        half.setOnClickListener(v -> {
            selectedDays = 15;
            updateButtonAppearance();
            loadRiskDataFromDatabase();
        });
        updateButtonAppearance();

        // 设置下拉刷新
        swipeRefresh.setOnRefreshListener(() -> {
            // 刷新数据，刷新状态会在数据加载完成后自动结束
            loadRiskDataFromDatabaseWithRefreshComplete();
        });
    }

    /** 加载数据并在下拉刷新完成后结束刷新状态 */
    private void loadRiskDataFromDatabaseWithRefreshComplete() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase database = AppDatabase.Companion.getDatabase(requireContext());
                @NotNull LocalDate fromDate = LocalDate.now().minusDays(selectedDays - 1);
                List<DailyRiskEntity> list = RiskRepository.INSTANCE.getFromBlocking(database.dailyRiskDao(), fromDate);

                // Reverse to show latest first
                List<DailyRiskEntity> result = new ArrayList<>(list);
                Collections.reverse(result);

                requireActivity().runOnUiThread(() -> {
                    riskDataList = result;
                    adapter.setList(result);
                    updateLineChartData(result);
                    swipeRefresh.setRefreshing(false); // 结束刷新状态
                });
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> swipeRefresh.setRefreshing(false));
            }
        });
        executor.shutdown();
    }

    private void updateButtonAppearance() {
        if (selectedDays == 7) {
            week.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.blue));
            half.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.deep_grey));
        } else {
            week.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.deep_grey));
            half.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.blue));
        }
    }

    private void initLineChart() {

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawGridBackground(false);

        Typeface tf = Typeface.createFromAsset(requireContext().getAssets(), "fonts/harmonyos_sans_regular.ttf");

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTypeface(tf);
        xAxis.setGranularity(1f);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setTypeface(tf);
        yAxis.setGranularity(0.1f);
        lineChart.getAxisRight().setEnabled(false);

        updateLineChartData(riskDataList);
    }

    private void initRecyclerView() {
        adapter = new RecordRVAdapter(new ArrayList<>());
        record.setLayoutManager(new LinearLayoutManager(getContext()));
        record.setAdapter(adapter);

        ItemSpacingDecoration itemSpacingDecoration = new ItemSpacingDecoration(getContext(), 3, 16, false);
        record.addItemDecoration(itemSpacingDecoration);

        adapter.setOnRecordClickListener((position, riskEntity) -> {

            FragmentManager fm = getParentFragmentManager();
            if (fm.findFragmentByTag("RiskDetailBottomSheet") != null) {
                return;
            }

            LocalDate date = riskEntity.getDate();
            RecordDetailBottomSheet sheet = RecordDetailBottomSheet.newInstance(date);
            sheet.show(getParentFragmentManager(), "RiskDetailBottomSheet");
        });

        loadRiskDataFromDatabase();
    }

    private void loadRiskDataFromDatabase() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase database = AppDatabase.Companion.getDatabase(requireContext());
                @NotNull LocalDate fromDate = LocalDate.now().minusDays(selectedDays - 1);
                List<DailyRiskEntity> list = RiskRepository.INSTANCE.getFromBlocking(database.dailyRiskDao(), fromDate);

                // Reverse to show latest first
                List<DailyRiskEntity> result = new ArrayList<>(list);
                Collections.reverse(result);

                requireActivity().runOnUiThread(() -> {
                    riskDataList = result;
                    adapter.setList(result);
                    updateLineChartData(result);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executor.shutdown();
    }

    private void updateLineChartData(List<DailyRiskEntity> list) {
        List<Entry> entries = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (int i = 1; i < list.size(); i++) {
                DailyRiskEntity entity = list.get(i);
                entries.add(new Entry(i, (float) entity.getRiskScore()));
            }
        }
        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.blue));
        dataSet.setLineWidth(1f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        // dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Set the mode to CUBIC_BEZIER

        lineChart.setData(new LineData(dataSet));

        // Update Y axis range and labels
        TreeSet<Float> yLabels = new TreeSet<>();
        for (Entry entry : entries) {
            float val = entry.getY();
            float floor = (float) (Math.floor(val * 10) / 10.0);
            float ceil = (float) (Math.ceil(val * 10) / 10.0);
            yLabels.add(floor);
            yLabels.add(ceil);
        }
        YAxis yAxis = lineChart.getAxisLeft();
        if (!yLabels.isEmpty()) {
            yAxis.setAxisMinimum(yLabels.first());
            yAxis.setAxisMaximum(yLabels.last());
            yAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format("%.1f", value);
                }
            });
        } else {
            // Set default range if no data
            yAxis.setAxisMinimum(0f);
            yAxis.setAxisMaximum(1f);
        }
        yAxis.setGranularityEnabled(true);

        lineChart.invalidate();
    }
}
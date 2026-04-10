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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cognitive.R;
import com.example.common.persistense.risk.DailyRiskEntity;
import com.example.common.record.ui.RecordDetailBottomSheet;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import mine.vm.RecordViewModel;

public class RecordFragment extends Fragment {

    private static final String TAG = "RecordFragment";

    private TextView week;
    private TextView half;
    private LineChart lineChart;
    private RecyclerView record;
    private SwipeRefreshLayout swipeRefresh;
    private RecordRVAdapter adapter;
    private List<DailyRiskEntity> riskDataList = new ArrayList<>();
    private int selectedDays = 15;

    private RecordViewModel viewModel;

    public RecordFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RecordViewModel.class);

        bindView(view);
        bindClickListener();
        initLineChart();
        initRecyclerView();
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getAllRiskData().observe(getViewLifecycleOwner(), list -> {
            if (list.isEmpty()) {
                return;
            }
            riskDataList = new ArrayList<>(list);
            adapter.setList(riskDataList);
            updateLineChartData(riskDataList);
            swipeRefresh.setRefreshing(false);
        });
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
            viewModel.queryRecordsByDays(7);
        });
        half.setOnClickListener(v -> {
            selectedDays = 15;
            updateButtonAppearance();
            viewModel.queryRecordsByDays(15);
        });
        updateButtonAppearance();

        swipeRefresh.setOnRefreshListener(() -> viewModel.queryRecordsByDays(selectedDays));
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

        com.example.common.util.ItemSpacingDecoration itemSpacingDecoration =
                new com.example.common.util.ItemSpacingDecoration(getContext(), 3, 16, false);
        record.addItemDecoration(itemSpacingDecoration);

        adapter.setOnRecordClickListener((position, riskEntity) -> {
            FragmentManager fm = getParentFragmentManager();
            if (fm.findFragmentByTag("RecordDetailBottomSheet") != null) {
                return;
            }
            LocalDate date = riskEntity.getDate();
            RecordDetailBottomSheet sheet = RecordDetailBottomSheet.newInstance(date);
            sheet.show(getParentFragmentManager(), "RecordDetailBottomSheet");
        });

        viewModel.queryRecordsByDays(selectedDays);
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

        lineChart.setData(new LineData(dataSet));

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
            yAxis.setAxisMinimum(0f);
            yAxis.setAxisMaximum(1f);
        }
        yAxis.setGranularityEnabled(true);

        lineChart.invalidate();
    }
}

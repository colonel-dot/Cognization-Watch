package com.example.common.record.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.common.R;
import com.example.common.persistense.risk.RiskLevel;
import com.example.common.record.vm.RecordViewModel;
import com.example.common.util.StringMap;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDate;

public class RecordDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String KEY_DATE = "date";

    public static RecordDetailBottomSheet newInstance(LocalDate date) {
        RecordDetailBottomSheet f = new RecordDetailBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DATE, date.toString());
        f.setArguments(bundle);
        return f;
    }

    private RecordViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_record_detail, container, false);

        TextView date = view.findViewById(R.id.date);
        TextView level = view.findViewById(R.id.level);
        TextView score = view.findViewById(R.id.score);
        TextView behavior = view.findViewById(R.id.behavior);
        TextView detail = view.findViewById(R.id.detail);

        Bundle args = getArguments();
        if (args == null) return view;

        String dateStr = args.getString(KEY_DATE);
        if (dateStr == null) return view;

        LocalDate label = LocalDate.parse(dateStr);

        viewModel = new ViewModelProvider(this).get(RecordViewModel.class);

        viewModel.queryRiskByDate(label);

        viewModel.getRiskData().observe(getViewLifecycleOwner(), risk -> {

            if (risk == null) {
                date.setText(label.toString());
                level.setText("暂无评估");
                score.setText("--");
                detail.setText("该日期尚未生成风险分析数据。");
            } else {
                date.setText(label.toString());
                level.setText(risk.getRiskLevel().toString());
                score.setText(Integer.toString((int)(risk.getRiskScore() * 100)));

                if (risk.getRiskLevel() == RiskLevel.认知能力有明显下滑趋势) {
                    level.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                } else if (risk.getRiskLevel() == RiskLevel.认知情况存在轻度风险) {
                    level.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange));
                } else if (risk.getRiskLevel() == RiskLevel.认知情况正常) {
                    level.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
                } else if (risk.getRiskLevel() == RiskLevel.数据不足无法评估) {
                    level.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                }

                String detailBuilder = "• 睡眠风险指数：" + String.format("%.2f", risk.getSleepRisk()) + "\n" +
                        "• 舒尔特风险指数：" + String.format("%.2f", risk.getSchulteRisk()) + "\n" +
                        "• 步数风险指数：" + String.format("%.2f", risk.getStepsRisk()) + "\n" +
                        "• 语音能力指数：" + String.format("%.2f", risk.getSpeechRisk());
                detail.setText(detailBuilder);

                TextView tvExplanations = view.findViewById(R.id.explanations);
                tvExplanations.setText("分析说明：\n" + risk.getExplanations());
            }
        });

        viewModel.getBehaviorData().observe(getViewLifecycleOwner(), behaviorEntity -> {
            if (behaviorEntity == null) {
                behavior.setText("该日期暂无行为数据。");
                return;
            }
            String behaviorBuilder = "• 起床时间：" + StringMap.mapMinuteOfDayToTime(behaviorEntity.getWakeMinute() != null ? behaviorEntity.getWakeMinute() : 0) + "\n" +
                    "• 睡觉时间：" + StringMap.mapMinuteOfDayToTime(behaviorEntity.getSleepMinute() != null ? behaviorEntity.getSleepMinute() : 0) + "\n" +
                    "• 舒尔特16格时间：" + String.format("%.2f", behaviorEntity.getSchulte16TimeSec() != null ? behaviorEntity.getSchulte16TimeSec() / 1000 : 0.0) + " 秒\n" +
                    "• 舒尔特25格时间：" + String.format("%.2f", behaviorEntity.getSchulte25TimeSec() != null ? behaviorEntity.getSchulte25TimeSec() / 1000 : 0.0) + " 秒\n" +
                    "• 语音评分：" + String.format("%.2f", behaviorEntity.getSpeechScore() != null ? behaviorEntity.getSpeechScore() : 0.0) + "\n" +
                    "• 步数：" + (behaviorEntity.getSteps() != null ? behaviorEntity.getSteps() : 0);
            behavior.setText(behaviorBuilder);
        });

        return view;
    }

    @Override
    public void onDestroy() {
        if (getDialog() != null) {
            getDialog().setOnCancelListener(null);
            getDialog().setOnDismissListener(null);
        }
        super.onDestroy();
    }
}

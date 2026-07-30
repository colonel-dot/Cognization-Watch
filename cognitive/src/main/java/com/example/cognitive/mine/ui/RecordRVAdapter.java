package com.example.cognitive.mine.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cognitive.R;
import com.example.cognitive.databinding.CognitiveItemRecordBinding;
import com.example.common.persistense.risk.DailyRiskEntity;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.common.util.StringMap;

public class RecordRVAdapter extends RecyclerView.Adapter<RecordRVAdapter.Holder> {

    private static final String TAG = "RecordRVAdapter";

    private List<DailyRiskEntity> list;

    public RecordRVAdapter(List<DailyRiskEntity> list) {
        this.list = list;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        CognitiveItemRecordBinding binding = CognitiveItemRecordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new Holder(binding);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateItem(DailyRiskEntity data) {
        Log.d(TAG, "Ready to update item for list: \n\t" + list);
        if (list == null || list.isEmpty()) {
            Log.d(TAG, "list is null or empty");
            return;
        }
        list.set(0, data);
        notifyItemChanged(0);
    }

    public void setList(List<DailyRiskEntity> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final CognitiveItemRecordBinding binding;

        public Holder(CognitiveItemRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindView(int position) {
            DailyRiskEntity item = list.get(position);
            binding.val.setText(String.valueOf((int)(item.getRiskScore() * 100)));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M.dd");
            String result = item.getDate().format(formatter);
            binding.date.setText(result);

            binding.label.setText(StringMap.mapDateToRelativeLabel(item.getDate()));

            if (listener != null) {
                itemView.setOnClickListener(v ->
                        listener.onRecordClick(position, item)
                );
            }
        }
    }

    private OnRecordClickListener listener;

    public void setOnRecordClickListener(OnRecordClickListener listener) {
        this.listener = listener;
    }

    public interface OnRecordClickListener {
        void onRecordClick(int position, DailyRiskEntity record);
    }
}
package com.example.cognitive.collection.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognitive.R;
import com.example.cognitive.databinding.ItemHealthMonitoringBinding;

import java.util.List;

import com.example.common.util.OnItemClickListener;
import com.example.common.util.StringMap;

import com.example.cognitive.collection.model.HealthMonitoringRVModel;

public class HealthMonitoringRVAdapter extends RecyclerView.Adapter<HealthMonitoringRVAdapter.Holder> {

    private final List<HealthMonitoringRVModel> list;

    public HealthMonitoringRVAdapter(List<HealthMonitoringRVModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHealthMonitoringBinding binding = ItemHealthMonitoringBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new Holder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final ItemHealthMonitoringBinding binding;

        public Holder(@NonNull ItemHealthMonitoringBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindView(int position) {
            HealthMonitoringRVModel item = list.get(position);
            binding.function.setText(item.getFunction());
            binding.data.setText(StringMap.mapNumberWithUnit(item.getData(), item.getUnit()));
            binding.progress.setProgress((int)(100 * item.getData() / item.getTarget()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            });
        }
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
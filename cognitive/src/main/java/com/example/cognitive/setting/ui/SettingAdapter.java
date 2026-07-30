package com.example.cognitive.setting.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognitive.R;
import com.example.cognitive.databinding.CognitiveItemSettingBinding;

import java.util.List;

import com.example.cognitive.setting.item.SettingItem;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.ViewHolder> {

    List<SettingItem> list;
    private final OnSettingsClickListener listener;

    public interface OnSettingsClickListener {
        void onItemClick(SettingItem item);
        void onSwitchChanged(SettingItem item, boolean isChecked);
    }

    public SettingAdapter(List<SettingItem> list, OnSettingsClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CognitiveItemSettingBinding binding = CognitiveItemSettingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final CognitiveItemSettingBinding binding;

        public ViewHolder(CognitiveItemSettingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindView(int position) {
            SettingItem item = list.get(position);
            binding.icon.setImageResource(item.getIcon());
            binding.type.setText(item.getType());
            if (item.isSwitch()) {
                binding.settingSwitch.setVisibility(View.VISIBLE);
                binding.expand.setVisibility(View.GONE);
                binding.settingSwitch.setChecked(item.isChecked());
                binding.settingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onSwitchChanged(item, isChecked));
            } else {
                binding.settingSwitch.setVisibility(View.GONE);
                binding.expand.setVisibility(View.VISIBLE);
                itemView.setOnClickListener(v -> listener.onItemClick(item));
            }
        }
    }
}
package com.example.cognitive.game.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognitive.R;
import com.example.cognitive.databinding.ItemBrainTrainingBinding;

import java.util.List;

import com.example.common.util.OnItemClickListener;

import com.example.cognitive.game.model.BrainTrainingRVModel;

public class BrainTrainingRVAdapter extends RecyclerView.Adapter<BrainTrainingRVAdapter.Holder> {
    List<BrainTrainingRVModel> list;

    public BrainTrainingRVAdapter(List<BrainTrainingRVModel> list) {
        super();
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBrainTrainingBinding binding = ItemBrainTrainingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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
        private final ItemBrainTrainingBinding binding;

        public Holder(@NonNull ItemBrainTrainingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindView(int position) {
            binding.function.setText(list.get(position).getFunction());
            binding.icon.setImageResource(list.get(position).getIcon());
            binding.state.setText(list.get(position).getState());
            itemView.setBackgroundTintList(
                    ContextCompat.getColorStateList(itemView.getContext(), list.get(position).getBackground())
            );

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
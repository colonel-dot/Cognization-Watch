package com.example.cognitive.main.home.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognitive.R;
import com.example.cognitive.databinding.ItemHomeBinding;

import java.util.List;

import com.example.cognitive.main.home.model.HomeRVModel;
import com.example.common.util.OnItemClickListener;

public class HomeRVAdapter extends RecyclerView.Adapter<HomeRVAdapter.Holder> {
    private static final String TAG = "HomeRVAdapter";
    List<HomeRVModel> list;

    public HomeRVAdapter(List<HomeRVModel> list) {
        super();
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeBinding binding = ItemHomeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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
        private final ItemHomeBinding binding;

        public Holder(ItemHomeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindView(int position) {
            binding.icon.setImageResource(list.get(position).getImage());
            binding.function.setText(list.get(position).getFunction());
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
package com.example.cognitive.schulte.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognitive.R;
import com.example.cognitive.databinding.ItemSchulteGridBinding;

import java.util.Collections;
import java.util.List;

import com.example.cognitive.schulte.data.SchulteGridCell;
import com.example.common.util.OnItemClickListener;

public class SchulteGridRVAdapter extends RecyclerView.Adapter<SchulteGridRVAdapter.Holder> {

    private final List<SchulteGridCell> list;

    public SchulteGridRVAdapter(List<SchulteGridCell> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSchulteGridBinding binding = ItemSchulteGridBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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

    public void shuffle() {
        Collections.shuffle(list);
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final ItemSchulteGridBinding binding;

        public Holder(@NonNull ItemSchulteGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindView(int position) {
            int num = list.get(position).getNum();
            binding.cell.setText(String.valueOf(num));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            });
        }
    }

    public List<SchulteGridCell> getList() {
        return list;
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
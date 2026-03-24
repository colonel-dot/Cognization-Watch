package com.example.cognitive.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognitive.R;

import java.util.List;

import util.OnItemClickListener;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView function;

        public Holder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            function = itemView.findViewById(R.id.function);
        }

        public void bindView(int position) {
            icon.setImageResource(list.get(position).getImage());
            function.setText(list.get(position).getFunction());
            itemView.setBackgroundTintList(
                    ContextCompat.getColorStateList(itemView.getContext(), list.get(position).getBackground())
            );

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(position);
                } else {
                }
            });
        }
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}

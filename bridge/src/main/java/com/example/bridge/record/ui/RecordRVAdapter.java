package com.example.bridge.record.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bridge.R;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.common.persistense.risk.DailyRiskEntity;
import com.example.common.util.StringMap;

public class RecordRVAdapter extends RecyclerView.Adapter<RecordRVAdapter.Holder> {

    private static final String TAG = "RecordRVAdapter";

    private List<DailyRiskEntity> list;

    public RecordRVAdapter(List<DailyRiskEntity> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record, parent, false);
        return new Holder(itemView);
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

        TextView val; // risk
        TextView date; // 3.17
        TextView label; // today, yesterday or Monday etc.

        public Holder(View itemView) {
            super(itemView);

            val = itemView.findViewById(R.id.val);
            date = itemView.findViewById(R.id.date);
            label = itemView.findViewById(R.id.label);
        }

        public void bindView(int position) {
            DailyRiskEntity item = list.get(position);
            val.setText(String.valueOf((int)(item.getRiskScore() * 100)));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M.dd");
            String result = item.getDate().format(formatter);
            date.setText(result);

            label.setText(StringMap.mapDateToRelativeLabel(item.getDate()));

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

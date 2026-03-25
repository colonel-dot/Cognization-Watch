package com.example.bridge.geofence.ui;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bridge.R;
import com.example.common.persistense.geofence.GeofenceItem;

import java.util.List;
import java.util.Calendar;

import com.example.bridge.util.OnItemClickListener;
import com.example.bridge.util.StringMap;

public class GeofenceRVAdapter extends RecyclerView.Adapter<GeofenceRVAdapter.Holder> {
    List<GeofenceItem> list;

    public GeofenceRVAdapter(List<GeofenceItem> list) {
        super();
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_geofence, parent, false);
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

    private int getMinuteOfDay(int timestamp) {
        long timeMillis = timestamp * 60L * 1000L;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeMillis);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        return hour * 60 + minute;
    }

    class Holder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView time;
        TextView status;

        public Holder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            time = itemView.findViewById(R.id.time);
            status = itemView.findViewById(R.id.status);
        }

        public void bindView(int position) {
            GeofenceItem item = list.get(position);

            if (StringMap.isDayTime(getMinuteOfDay(item.getTimestamp()))) {
                icon.setImageResource(R.drawable.sun);
            } else {
                icon.setImageResource(R.drawable.moon);
            }

            if (item.getStatus() == GeofenceItem.STATUS_IN) {
                status.setText("进入守护范围");
                icon.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(icon.getContext(), R.color.light_green))
                );
                icon.setImageTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(icon.getContext(), R.color.green))
                );
            } else if (item.getStatus() == GeofenceItem.STATUS_OUT) {
                status.setText("离开守护范围");
                icon.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(icon.getContext(), R.color.light_red))
                );
                icon.setImageTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(icon.getContext(), R.color.red))
                );
            } else if (item.getStatus() == GeofenceItem.STATUS_STAYED) {
                status.setText("停留在守护范围10分钟");
                icon.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(icon.getContext(), R.color.light_orange))
                );
                icon.setImageTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(icon.getContext(), R.color.orange))
                );
            }
            else if (item.getStatus() == GeofenceItem.STATUS_LOCFAIL) {
                status.setText("位置无法获取");
                icon.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(icon.getContext(), R.color.light_red))
                );
                icon.setImageTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(icon.getContext(), R.color.red))
                );
            } else {
                status.setText("状态不明");
                icon.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(icon.getContext(), R.color.light_red))
                );
                icon.setImageTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(icon.getContext(), R.color.red))
                );
            }

            time.setText(StringMap.mapMinuteToRelativeTime(item.getTimestamp()));

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

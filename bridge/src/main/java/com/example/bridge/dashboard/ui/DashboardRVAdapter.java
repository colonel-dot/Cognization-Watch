package com.example.bridge.dashboard.ui;

import static com.example.bridge.dashboard.item.DashboardItem.TYPE_ALERT;
import static com.example.bridge.dashboard.item.DashboardItem.TYPE_COLLECTION;
import static com.example.bridge.dashboard.item.DashboardItem.TYPE_RISK;
import static com.example.bridge.dashboard.item.DashboardItem.TYPE_RTC;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bridge.R;
import com.example.bridge.dashboard.item.DashboardAlertItem;
import com.example.bridge.dashboard.item.DashboardCollectionItem;
import com.example.bridge.dashboard.item.DashboardItem;
import com.example.bridge.dashboard.item.DashboardRiskItem;
import com.example.bridge.dashboard.item.DashboardRtcItem;

import java.util.ArrayList;
import java.util.List;

import com.example.common.util.GenerateAutoAvatar;
import com.example.common.util.OnItemClickListener;
import com.example.common.util.StringMap;

public class DashboardRVAdapter extends RecyclerView.Adapter<DashboardRVAdapter.Holder> {

    List<DashboardItem> list = new ArrayList<>();

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getViewType();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case TYPE_RTC:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_rtc, parent, false);
                return new RtcHolder(view);
            case TYPE_RISK:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_risk, parent, false);
                return new RiskHolder(view);
            case TYPE_COLLECTION:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_collection, parent, false);
                return new CollectionHolder(view);
            case TYPE_ALERT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_alert, parent, false);
                return new AlertHolder(view);
        }
        return new RtcHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    abstract class Holder extends RecyclerView.ViewHolder {

        private Holder(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void bindView(int position);
    }

    class RtcHolder extends Holder {

        ImageView portrait;
        TextView from;
        TextView status;
        ImageView answer;

        public RtcHolder(@NonNull View itemView) {
            super(itemView);
            portrait = itemView.findViewById(R.id.portrait);
            from = itemView.findViewById(R.id.from);
            status = itemView.findViewById(R.id.status);
            answer = itemView.findViewById(R.id.answer);
        }

        @Override
        public void bindView(int position) {
            DashboardRtcItem dri = (DashboardRtcItem) list.get(position);
            from.setText(dri.getName());
            status.setText("状态：" + dri.getStatus());

            answer.setOnClickListener(v -> {
                if (rtcListener != null) {
                    rtcListener.onItemClick(position);
                }
            });

            portrait.post(() -> {
                portrait.setImageBitmap(
                        GenerateAutoAvatar.generate(dri.getName(), portrait.getHeight())
                );
            });
        }
    }

    class RiskHolder extends Holder {

        TextView assess; // val background
        TextView val; // val
        TextView trend;

        public RiskHolder(@NonNull View itemView) {
            super(itemView);
            assess = itemView.findViewById(R.id.assess);
            val = itemView.findViewById(R.id.val);
            trend = itemView.findViewById(R.id.trend);
        }

        @Override
        public void bindView(int position) {
            DashboardRiskItem dri = (DashboardRiskItem) list.get(position);
            int risk = (int)(dri.getRisk() * 100);

            if (risk >= 70) {
                assess.setBackgroundTintList(
                        ColorStateList.valueOf(
                                ContextCompat.getColor(assess.getContext(), R.color.red)
                        )
                );
                assess.setText("高风险");
                val.setTextColor(ContextCompat.getColor(assess.getContext(), R.color.red));
            } else if (risk < 70 && risk >= 40) {
                assess.setBackgroundTintList(
                        ColorStateList.valueOf(
                                ContextCompat.getColor(assess.getContext(), R.color.orange)
                        )
                );
                assess.setText("中等风险");
                val.setTextColor(ContextCompat.getColor(assess.getContext(), R.color.orange));
            } else if (risk < 40 && risk >= 0) {
                assess.setBackgroundTintList(
                        ColorStateList.valueOf(
                                ContextCompat.getColor(assess.getContext(), R.color.cyan)
                        )
                );
                assess.setText("低风险");
                val.setTextColor(ContextCompat.getColor(assess.getContext(), R.color.cyan));
            }

            val.setText(String.valueOf(risk));

            if (dri.getYesterday() == 0) {
                trend.setText("没有数据源");
            } else {
                trend.setText((int) Math.round(
                        (dri.getRisk() - dri.getYesterday()) / dri.getYesterday() * 100) + "% 据上周增长");
            }
        }
    }

    class CollectionHolder extends Holder {

        TextView step;
        TextView sleep;

        public CollectionHolder(@NonNull View itemView) {
            super(itemView);
            step = itemView.findViewById(R.id.step);
            sleep = itemView.findViewById(R.id.sleep);
        }

        @Override
        public void bindView(int position) {
            DashboardCollectionItem dci = (DashboardCollectionItem) list.get(position);
            step.setText(StringMap.mapNumberWithoutUnit(dci.getStep()));
            sleep.setText(StringMap.mapNumberWithoutUnit(dci.getSleep()));
        }
    }

    class AlertHolder extends Holder {

        TextView introduce;
        LinearLayout tip;

        public AlertHolder(@NonNull View itemView) {
            super(itemView);
            introduce = itemView.findViewById(R.id.introduce);
            tip = itemView.findViewById(R.id.tip);
        }

        @Override
        public void bindView(int position) {
            DashboardAlertItem dai = (DashboardAlertItem) list.get(position);
            introduce.setText(dai.getTip());

            tip.setOnClickListener(v -> {
                if (alertListener != null) {
                    alertListener.onItemClick(position);
                }
            });
        }
    }

    private OnItemClickListener rtcListener;
    private OnItemClickListener alertListener;

    public void setOnRtcClickListener(OnItemClickListener rtcListener) {
        this.rtcListener = rtcListener;
    }

    public void setOnAlertClickListener(OnItemClickListener alertListener) {
        this.alertListener = alertListener;
    }
}

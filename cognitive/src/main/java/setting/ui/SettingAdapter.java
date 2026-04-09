package setting.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cognitive.R;

import java.util.List;

import setting.item.SettingItem;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.ViewHolder> {

    List<SettingItem> list;
    private OnSettingsClickListener listener;

    public interface OnSettingsClickListener {
        void onItemClick(SettingItem item);
        void onSwitchChanged(SettingItem item, boolean isChecked);
    }

    public SettingAdapter(List<SettingItem> list, OnSettingsClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_setting, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView icon_iv;
        TextView type_tv;
        Switch switch_;
        ImageView expand_iv;

        public ViewHolder(View itemView) {
            super(itemView);
            icon_iv = itemView.findViewById(R.id.icon);
            type_tv = itemView.findViewById(R.id.type);
            switch_ = itemView.findViewById(R.id.switch_);
            expand_iv = itemView.findViewById(R.id.expand);
        }

        public void bindView(int position) {
            SettingItem item = list.get(position);
            icon_iv.setImageResource(item.getIcon());
            type_tv.setText(item.getType());
            if (item.isSwitch()) {
                switch_.setVisibility(View.VISIBLE);
                expand_iv.setVisibility(View.GONE);
                switch_.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    listener.onSwitchChanged(item, isChecked);
                });
            } else {
                switch_.setVisibility(View.GONE);
                expand_iv.setVisibility(View.VISIBLE);
                itemView.setOnClickListener(v -> {
                    listener.onItemClick(item);
                });
            }
        }
    }
}
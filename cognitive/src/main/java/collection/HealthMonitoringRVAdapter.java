package collection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognitive.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

import com.example.common.util.OnItemClickListener;
import com.example.common.util.StringMap;

public class HealthMonitoringRVAdapter extends RecyclerView.Adapter<HealthMonitoringRVAdapter.Holder> {

    private List<HealthMonitoringRVModel> list;

    public HealthMonitoringRVAdapter(List<HealthMonitoringRVModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health_monitoring, parent, false);
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

        TextView function;
        TextView data;
        LinearProgressIndicator progress;

        public Holder(@NonNull View itemView) {
            super(itemView);
            function = itemView.findViewById(R.id.function);
            data = itemView.findViewById(R.id.data);
            progress = itemView.findViewById(R.id.progress);
        }

        public void bindView(int position) {
            HealthMonitoringRVModel item = list.get(position);
            function.setText(item.getFunction());
            data.setText(StringMap.mapNumberWithUnit(item.getData(), item.getUnit()));
            progress.setProgress((int)(100 * item.getData() / item.getTarget()));

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

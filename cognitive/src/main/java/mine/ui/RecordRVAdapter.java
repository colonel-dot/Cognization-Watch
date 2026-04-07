package mine.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cognitive.R;
import com.example.common.persistense.risk.DailyRiskEntity;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.common.util.StringMap;

public class RecordRVAdapter extends RecyclerView.Adapter<RecordRVAdapter.Holder> {

    private static final String TAG = "RecordRVAdapter";

    private List<DailyRiskEntity> list;

    public RecordRVAdapter(List<DailyRiskEntity> list) {
        this.list = list;
    }

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

//        TextView read;
//        TextView schulte;
//        TextView steps;
//        TextView schedule;
        TextView val; // risk
        TextView date; // 3.17
        TextView label; // today, yesterday or Monday etc.

        public Holder(View itemView) {
            super(itemView);

//            read = itemView.findViewById(R.id.read);
//            schulte = itemView.findViewById(R.id.schulte);
//            steps = itemView.findViewById(R.id.steps);
//            schedule = itemView.findViewById(R.id.schedule);
            val = itemView.findViewById(R.id.val);
            date = itemView.findViewById(R.id.date);
            label = itemView.findViewById(R.id.label);
        }

        public void bindView(int position) {
            DailyRiskEntity item = list.get(position);
//            read.setText(
//                    "语音评分:" + (item.getSpeechScore() != null ? item.getSpeechScore() : "暂无")
//            );
//            schulte.setText(
//                    "舒尔特成绩:" +
//                    (item.getSchulte16TimeSec() != null
//                            ? (item.getSchulte16TimeSec() / 1000) + "秒(4×4)"
//                            : "暂无")
//                    + " " +
//                    (item.getSchulte25TimeSec() != null
//                            ? (item.getSchulte25TimeSec() / 1000) + "秒(5×5)"
//                            : "暂无")
//            );
//            steps.setText(
//                    "步数:" + (item.getSteps() != null ? item.getSteps() : "暂无")
//            );
//            schedule.setText("起床:" + StringMap.mapMinuteToTime(item.getWakeMinute()) +
//                            " 睡觉:" + StringMap.mapMinuteToTime(item.getSleepMinute()));
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

package game.ui;

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

import com.example.common.util.OnItemClickListener;

import game.model.BrainTrainingRVModel;

public class BrainTrainingRVAdapter extends RecyclerView.Adapter<BrainTrainingRVAdapter.Holder> {
    List<BrainTrainingRVModel> list;

    public BrainTrainingRVAdapter(List<BrainTrainingRVModel> list) {
        super();
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brain_training, parent, false);
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

    public class Holder extends RecyclerView.ViewHolder {

        TextView function;
        ImageView icon;
        TextView state;

        public Holder(@NonNull View itemView) {
            super(itemView);
            function = itemView.findViewById(R.id.function);
            icon = itemView.findViewById(R.id.icon);
            state = itemView.findViewById(R.id.state);
        }

        public void bindView(int position) {
            function.setText(list.get(position).getFunction());
            icon.setImageResource(list.get(position).getIcon());
            state.setText(list.get(position).getState());
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
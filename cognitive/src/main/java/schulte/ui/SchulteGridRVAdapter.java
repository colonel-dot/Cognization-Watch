package schulte.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognitive.R;

import java.util.Collections;
import java.util.List;

import schulte.data.SchulteGridCell;
import com.example.common.util.OnItemClickListener;

public class SchulteGridRVAdapter extends RecyclerView.Adapter<SchulteGridRVAdapter.Holder> {

    private List<SchulteGridCell> list;

    public SchulteGridRVAdapter(List<SchulteGridCell> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schulte_grid, parent, false);
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

    public void shuffle() {
        Collections.shuffle(list);
        notifyDataSetChanged();
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView cell;

        public Holder(@NonNull View itemView) {
            super(itemView);
            cell = itemView.findViewById(R.id.cell);
        }

        public void bindView(int position) {
            int num = list.get(position).getNum();
            cell.setText(String.valueOf(num));

//            if (list.get(position).isSelected()) {
//
//                cell.setAlpha(1f);
//                cell.animate()
//                        .alpha(0f)
//                        .setDuration(100)
//                        .start();
//
//            } else {
//
//                cell.setAlpha(1f);
//            }

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

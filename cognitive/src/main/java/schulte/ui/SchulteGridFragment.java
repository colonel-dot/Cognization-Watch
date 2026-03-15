package schulte.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cognitive.R;

import java.util.ArrayList;
import java.util.List;

import read_assessment.vm.RecordViewModel;
import schulte.data.SchulteGridCell;
import schulte.engine.SchulteGridEngine;
import schulte.vm.SchulteGameViewModel;
import util.TimerHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SchulteGridFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SchulteGridFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SchulteGridFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SchulteGridFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SchulteGridFragment newInstance(String param1, String param2) {
        SchulteGridFragment fragment = new SchulteGridFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schulte_grid, container, false);
    }

    private TextView time;
    private TextView grid;
    private RecyclerView schulte;
    private TextView pause;
    private TextView start;

    private TimerHelper timer;
    private SchulteGridEngine engine;
    private SchulteGridRVAdapter adapter;

    private SchulteGameViewModel viewModel;

    private Long ms = 0l;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SchulteGameViewModel.class);

        bindView(view);

        timer = new TimerHelper();
        timer.setOnTimerListener(val -> {
            ms = val;
            int second = Math.toIntExact(val / 1000);
            time.setText(Integer.toString(second));
        });

        engine = new SchulteGridEngine();

        List<SchulteGridCell> list = new ArrayList<>();
        for (int i = 1; i <= engine.getEnd(); i++) {
            list.add(new SchulteGridCell(i));
        }
        adapter = new SchulteGridRVAdapter(list);
        schulte.setAdapter(adapter);
        adapter.setOnItemClickListener(pos -> {
            int res = engine.click(adapter.getList().get(pos).getNum());
            if (res == -1) { // false
                // TODO: 失败匹配动效
            } else if (res == 1) { // complete
                timer.stop();
                engine.stop();
                start.setText("START");
                pause.setText("PAUSE");
                // TODO: 利用 ms 变量毫秒数参与算法获得结果, 胜利动效
                showFinishedDialog();
                viewModel.saveGameTime(engine.isFourSquared() ? 4 : 5, ms);
            } else if (res == 0) { // continue
//                adapter.getList().get(pos).setSelected(true);
//                adapter.notifyItemChanged(pos);
                grid.setText(engine.getCur() + " / " + engine.getEnd());
            }
        });
        int span = engine.isFourSquared() ? 4 : 5;
        schulte.setLayoutManager(new GridLayoutManager(requireContext(), span));

        bindClickListener();
    }

    public void bindView(View view) {
        time = view.findViewById(R.id.time);
        grid = view.findViewById(R.id.grid);
        schulte = view.findViewById(R.id.schulte);
        pause = view.findViewById(R.id.pause);
        start = view.findViewById(R.id.start);
    }

    public void bindClickListener() {
        pause.setOnClickListener(v -> {
            if (engine.getState() == SchulteGridEngine.State.RUNNING) {
                timer.pause();
                engine.pause();
                pause.setText("RESUME");
            } else if (engine.getState() == SchulteGridEngine.State.PAUSED) {
                timer.resume();
                engine.resume();
                pause.setText("PAUSE");
            }
        });
        start.setOnClickListener(v -> {
            if (engine.getState() == SchulteGridEngine.State.STOPPED) {
                timer.start();
                engine.start();
                adapter.shuffle();
                start.setText("STOP");
            } else {
                timer.stop();
                engine.stop();
                start.setText("START");
                pause.setText("PAUSE");

                time.setText("0");
                grid.setText("0 / " + engine.getEnd());

                for (SchulteGridCell cell : adapter.getList()) {
                    cell.setSelected(false);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showFinishedDialog() {
        double seconds = ms / 1000.0;

        new AlertDialog.Builder(requireContext())
                .setTitle("完成！")
                .setMessage(String.format("用时 %.3f 秒", seconds))
                .setPositiveButton("确定", (dialog, which) -> {
                    time.setText("0");
                    grid.setText("0 / " + engine.getEnd());
                    dialog.dismiss();
                })
                .show();
    }
}
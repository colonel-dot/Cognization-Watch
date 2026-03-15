package game;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cognitive.R;

import java.util.ArrayList;
import java.util.List;

import schulte.ui.SchulteGridFragment;
import util.ItemSpacingDecoration;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BrainTrainingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BrainTrainingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BrainTrainingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BrainTrainingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BrainTrainingFragment newInstance(String param1, String param2) {
        BrainTrainingFragment fragment = new BrainTrainingFragment();
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
        return inflater.inflate(R.layout.fragment_brain_training, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<BrainTrainingRVModel> list = new ArrayList<>();
        list.add(new BrainTrainingRVModel("Schulte Grid", R.drawable.brain, "Completed", R.color.blue));
        list.add(new BrainTrainingRVModel("Sudoku", R.drawable.brain, "Progressing", R.color.green));
        list.add(new BrainTrainingRVModel("Number Sequence", R.drawable.brain, "Completed", R.color.blue));
        list.add(new BrainTrainingRVModel("Shape Recognition", R.drawable.brain, "None-Started", R.color.orange));

        BrainTrainingRVAdapter adapter = new BrainTrainingRVAdapter(list);
        RecyclerView recyclerView = view.findViewById(R.id.content);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            Fragment fragment = null;

            switch (position) {
                case 0:
                    fragment = new SchulteGridFragment();
                    break;
                case 1:
                    // fragment = new SudokuFragment();
                    break;
                // ...
            }

            if (fragment != null) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        ItemSpacingDecoration itemSpacingDecoration = new ItemSpacingDecoration(getContext(), 20, false);
        recyclerView.addItemDecoration(itemSpacingDecoration);
    }
}
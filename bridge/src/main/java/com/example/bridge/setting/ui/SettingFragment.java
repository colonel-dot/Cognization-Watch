package com.example.bridge.setting.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bridge.R;
import com.example.bridge.setting.item.SettingItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
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
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    private RecyclerView content;
    private LinearLayout signout;

    private SettingAdapter adapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindView(view);
        initRVAdapter();
        initListener();
    }

    private void bindView(View view) {
        content = view.findViewById(R.id.content);
        signout = view.findViewById(R.id.signout);
    }

    private void initRVAdapter() {
        List<SettingItem> items = new ArrayList<>();
        items.add(new SettingItem(R.drawable.profiles, "设置绑定用户备注", 0, true));
        items.add(new SettingItem(R.drawable.map, "重设围栏", 1, false));

        SettingAdapter adapter = new SettingAdapter(items, new SettingAdapter.OnSettingsClickListener() {
            @Override
            public void onItemClick(SettingItem item) {
                switch (item.getPosition()) {
                    case 0:
                        // TODO: 触发弹窗
                        break;
                    case 1:
                        // TODO: 触发弹窗
                        break;
                }
            }

            @Override
            public void onSwitchChanged(SettingItem item, boolean isChecked) {
                switch (item.getPosition()) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
            }
        });

        content.setLayoutManager(new LinearLayoutManager(requireContext()));
        content.setAdapter(adapter);
    }

    private void initListener() {
        signout.setOnClickListener(v -> {
            // TODO: sign out
        });
    }
}
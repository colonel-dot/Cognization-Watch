package com.example.cognitive.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.cognitive.R;

import java.util.ArrayList;
import java.util.List;

import collection.HealthMonitoringFragment;
import read_assessment.ui.RecordFragment;
import game.BrainTrainingFragment;
import rtc.VideoCallFragment;
import sports.data.StepForegroundService;
import util.ItemSpacingDecoration;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private final ActivityResultLauncher<String[]> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        boolean allGranted = true;
                        for (Boolean isGranted : permissions.values()) {
                            if (!isGranted) {
                                allGranted = false;
                                break;
                            }
                        }
                        if (allGranted) {
                            startStepService();
                        } else {
                            Toast.makeText(requireContext(), "通知权限未授予，可能无法正常接收步数提醒", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

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
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (checkPermissions()) {

        } else {
            cameraPermissionLauncher.launch(getRequiredPermissions());
        }

        List<HomeRVModel> list = new ArrayList<>();
        list.add(new HomeRVModel(R.drawable.brain, "Cognitive Test", R.color.blue));
        list.add(new HomeRVModel(R.drawable.game, "Brain Training", R.color.green));
        list.add(new HomeRVModel(R.drawable.pulse, "Health Monitoring", R.color.blue));
        list.add(new HomeRVModel(R.drawable.video, "Video Call", R.color.orange));

        HomeRVAdapter adapter = new HomeRVAdapter(list);
        RecyclerView recyclerView = view.findViewById(R.id.content);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            Fragment fragment = null;

            switch (position) {
                case 0:
                    fragment = new RecordFragment();
                    break;
                case 1:
                    fragment = new BrainTrainingFragment();
                    break;
                case 2:
                    fragment = new HealthMonitoringFragment();
                    break;
                case 3:
                    fragment = new VideoCallFragment();
            }

            if (fragment != null) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

         ItemSpacingDecoration itemSpacingDecoration = new ItemSpacingDecoration(getContext(), 24, false);
        recyclerView.addItemDecoration(itemSpacingDecoration);
    }

    private String[] getRequiredPermissions() {
        return new String[]{
                Manifest.permission.POST_NOTIFICATIONS,
        };
    }

    private boolean checkPermissions() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startStepService() {
        Intent intent = new Intent(requireContext(), StepForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 8.0+ 必须用这个启动前台服务
            requireContext().startForegroundService(intent);
        } else {
            // 8.0以下用旧方法
            requireContext().startService(intent);
        }
    }
}
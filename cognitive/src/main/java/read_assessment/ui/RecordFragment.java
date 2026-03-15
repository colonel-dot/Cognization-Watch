package read_assessment.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cognitive.R;

import java.io.File;

import read_assessment.vm.RecordViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RecordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordFragment newInstance(String param1, String param2) {
        RecordFragment fragment = new RecordFragment();
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
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    private final static String TAG = "RecordFragment";

    private TextView read;
    private ImageView mic;
    private TextView start;
    private TextView stop;
    private TextView result;

    private String speakText;

    private RecordViewModel viewModel;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startRecord();
                } else {
                    Toast.makeText(requireContext(), "没有录音权限", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RecordViewModel.class);

        bindView(view);

        observeViewModel();

        bindClickListener();
    }

    private void bindView(View view) {
        read = view.findViewById(R.id.read);
        mic = view.findViewById(R.id.mic);
        start = view.findViewById(R.id.start);
        stop = view.findViewById(R.id.stop);
        result = view.findViewById(R.id.result);

        speakText = viewModel.getText();
        read.setText(speakText);

        View bar_1 = view.findViewById(R.id.bar_1);
        View bar_2 = view.findViewById(R.id.bar_2);
        View bar_3 = view.findViewById(R.id.bar_3);
        View bar_4 = view.findViewById(R.id.bar_4);
        View bar_5 = view.findViewById(R.id.bar_5);

        startVoiceAnim(bar_1, 0);
        startVoiceAnim(bar_2, 120);
        startVoiceAnim(bar_3, 240);
        startVoiceAnim(bar_4, 120);
        startVoiceAnim(bar_5, 0);
    }

    public void bindClickListener() {
        read.setOnClickListener(v -> {
            speakText = viewModel.getText();
            Log.d(TAG, "换过来的句子是: " + speakText);
            read.setText(speakText);
            Log.d(TAG, "TextView 现在是: " + read.getText().toString());
        });
        start.setOnClickListener(v -> {
            if (checkPermissions()) {
                startRecord();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });
        stop.setOnClickListener(v -> {
            viewModel.stopRecord();
        });
        result.setOnClickListener(v -> {
            viewModel.evaluateSpeech(
                    speakText,
                    "zh-CHS"
            );
        });
    }

    private void startVoiceAnim(View bar, int delay) {

        ObjectAnimator animator = ObjectAnimator.ofFloat(bar, "scaleY", 0.4f, 1f);

        animator.setDuration(500);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setStartDelay(delay);

        animator.start();
    }

    private void observeViewModel() {
        viewModel.isRecording().observe(getViewLifecycleOwner(), isRec -> {
            start.setEnabled(!isRec);
            stop.setEnabled(isRec);
        });

        viewModel.getScoreResult().observe(getViewLifecycleOwner(), score -> {
            result.setText(String.valueOf(score));
        });

        viewModel.getRecordSavedEvent().observe(getViewLifecycleOwner(), file -> {
            if (file != null) {
                Toast.makeText(requireContext(), "录音已保存 " + file, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String[] getRequiredPermissions() {
        return new String[]{
                Manifest.permission.RECORD_AUDIO,
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

    private void startRecord() {
        File dir = requireContext().getExternalFilesDir(null);
        if (dir != null) {
            viewModel.startRecord(dir);
        }
        Toast.makeText(requireContext(), "开始录音", Toast.LENGTH_SHORT).show();
    }
}
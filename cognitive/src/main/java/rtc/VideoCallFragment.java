package rtc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.cognitive.R;
import com.example.common.rtc.RtcActivity;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoCallFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class VideoCallFragment extends Fragment {
    private static final String TAG = "VideoCallFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private PreviewView previewView;

    public VideoCallFragment() {
        // Required empty public constructor
    }
/**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideoCallFragment.
     */

    // TODO: Rename and change types and number of parameters
    public static VideoCallFragment newInstance(String param1, String param2) {
        VideoCallFragment fragment = new VideoCallFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private static final int PERMISSION_REQ_ID = 22;

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
                            startPreview();
                        }
                        // 权限被拒绝
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
        return inflater.inflate(R.layout.fragment_video_call, container, false);
    }

    private View call;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewView = view.findViewById(R.id.preview);

        previewView.setImplementationMode(
                PreviewView.ImplementationMode.COMPATIBLE
        ); // Texture supports rounded corners

        if (checkPermissions()) {
            startPreview();
        } else {
            cameraPermissionLauncher.launch(getRequiredPermissions());
        }

        call = view.findViewById(R.id.call);
        call.setOnClickListener(v -> {
            // TODO - 需要获取实际用户ID
            // 临时使用占位符，确保键名正确
            Intent intent = new Intent(getContext(), RtcActivity.class);
            intent.putExtra("userId", "test_elder_user");
            intent.putExtra("targetId", "test_child_user");
            intent.putExtra("isElder", true);
            startActivity(intent);
        });
    }

    private void startPreview() {
        if (previewView == null) {
            return;
        }
        // ↓ TODO
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                // CameraSelector.DEFAULT_BACK_CAMERA 后摄

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview
                );

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private String[] getRequiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
//                    Manifest.permission.READ_PHONE_STATE,
//                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };
        }
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
}

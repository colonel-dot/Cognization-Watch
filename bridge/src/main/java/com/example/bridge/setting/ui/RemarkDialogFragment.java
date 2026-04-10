package com.example.bridge.setting.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.bridge.R;
import com.example.common.bind_device.BindStatusManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RemarkDialogFragment extends DialogFragment {

    private TextInputEditText remarkUsername;
    private MaterialButton remarkButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            android.view.Window window = getDialog().getWindow();
            android.view.WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (320 * getResources().getDisplayMetrics().density);
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_remark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        remarkUsername = view.findViewById(R.id.remark_username);
        remarkButton = view.findViewById(R.id.remark_button);

        remarkButton.setOnClickListener(v -> {
            String remark = remarkUsername.getText() != null ? remarkUsername.getText().toString().trim() : null;
            if (remark != null && remark.isBlank()) {
                Toast.makeText(requireContext(), "请输入备注", Toast.LENGTH_SHORT).show();
                return;
            }
            BindStatusManager.INSTANCE.saveBindRemark(requireContext(), remark);
            Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}

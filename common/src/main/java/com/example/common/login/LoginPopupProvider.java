package com.example.common.login;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.example.common.bind_device.BindStatusManager;
import com.example.common.router.RouterPaths;

import com.example.common.bind_device.BindDialogFragment;

@Route(path = RouterPaths.POPUP_LOGIN)
public class LoginPopupProvider implements IProvider {

    private Context context;

    @Override
    public void init(Context context) {
        this.context = context;
    }

    public void showPopup() {
        if (!(context instanceof FragmentActivity)) return;

        BindStatusManager.INSTANCE.init(context);
        if (BindStatusManager.INSTANCE.isBound(context)) {
            return;
        }

        FragmentActivity activity = (FragmentActivity) context;
        BindDialogFragment dialog = new BindDialogFragment();
        dialog.show(activity.getSupportFragmentManager(), "BindDialog");
    }
}

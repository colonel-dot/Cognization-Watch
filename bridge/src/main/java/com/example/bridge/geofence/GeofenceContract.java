package com.example.bridge.geofence;

import com.example.bridge.base.BasePresenter;
import com.example.bridge.base.BaseView;
import com.tencent.trtc.TRTCCloud;

public interface GeofenceContract {
    interface View extends BaseView {
    }

    interface Presenter extends BasePresenter<View> {
    }
}

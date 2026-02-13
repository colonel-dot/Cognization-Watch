package com.example.bridge.rtc;

import com.example.bridge.base.BasePresenter;
import com.example.bridge.base.BaseView;
import com.tencent.trtc.TRTCCloud;

public interface RtcContract {
    interface View extends BaseView {
        void startLocalPreview(TRTCCloud cloud);
    }

    interface Presenter extends BasePresenter<RtcContract.View> {
    }
}

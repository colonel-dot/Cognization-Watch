package com.example.cogwatch.startup

import android.content.Context
import android.util.Log
import androidx.startup.Initializer

/**
 * TRTC SDK initialization placeholder.
 *
 * TRTC (Tencent Real-Time Communication) is already lazily initialized:
 * TRTCCloud.sharedInstance() is called on-demand when the user enters a video call
 * (RtcActivity / RtcManager). No pre-init is performed here to preserve cold-start
 * performance. This Initializer exists for architecture consistency and as a hook
 * for future SDK pre-warming if TRTC provides a lightweight preload API.
 */
class TrtcPreloadInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Log.d("Startup", "TRTC preload skipped — SDK is lazily initialized in RtcManager")
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

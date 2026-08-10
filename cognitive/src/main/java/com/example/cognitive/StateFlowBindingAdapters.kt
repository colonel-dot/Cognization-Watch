package com.example.cognitive

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


object StateFlowBindingAdapters {

    private val TAG_STATE_TEXT_JOB = "StateFlowBindingAdapters.stateTextJob".hashCode()
    private val TAG_STATE_ENABLED_JOB = "StateFlowBindingAdapters.stateEnabledJob".hashCode()
    private val TAG_STATE_DISABLED_JOB = "StateFlowBindingAdapters.stateDisabledJob".hashCode()


    @BindingAdapter("stateText")
    @JvmStatic
    fun bindStateText(view: TextView, flow: StateFlow<String>?) {
        if (flow == null) {
            cancelJob(view, TAG_STATE_TEXT_JOB)
            return
        }
        bindFlow(view, flow, TAG_STATE_TEXT_JOB) { value -> view.text = value }
    }


    @BindingAdapter("stateEnabled")
    @JvmStatic
    fun bindStateEnabled(view: View, flow: StateFlow<Boolean>?) {
        if (flow == null) {
            cancelJob(view, TAG_STATE_ENABLED_JOB)
            return
        }
        bindFlow(view, flow, TAG_STATE_ENABLED_JOB) { value -> view.isEnabled = value }
    }


    @BindingAdapter("stateDisabled")
    @JvmStatic
    fun bindStateDisabled(view: View, flow: StateFlow<Boolean>?) {
        if (flow == null) {
            cancelJob(view, TAG_STATE_DISABLED_JOB)
            return
        }
        bindFlow(view, flow, TAG_STATE_DISABLED_JOB) { value -> view.isEnabled = !value }
    }

    // ── internal ──────────────────────────────────────────────

    private fun <T> bindFlow(
        view: View,
        flow: StateFlow<T>,
        tagKey: Int,
        onValue: (T) -> Unit
    ) {
        // Cancel any previous job for this adapter on this view
        cancelJob(view, tagKey)

        val owner = view.findViewTreeLifecycleOwner()
        if (owner == null) {
            // View not yet attached — defer until it is
            view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    view.removeOnAttachStateChangeListener(this)
                    bindFlow(view, flow, tagKey, onValue)
                }

                override fun onViewDetachedFromWindow(v: View) {}
            })
            return
        }

        val job: Job = owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect { value -> onValue(value) }
            }
        }
        view.setTag(tagKey, job)
    }

    private fun cancelJob(view: View, tagKey: Int) {
        val prev = view.getTag(tagKey)
        if (prev is Job) {
            prev.cancel()
            view.setTag(tagKey, null)
        }
    }
}

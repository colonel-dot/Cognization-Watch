package com.example.cognitive.main

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import com.example.cognitive.R
import mine.ui.MineRecordFragment
import read_assessment.ui.RecordActivity
import schedule.ui.ScheduleActivity
import schulte.ui.SchulteGameActivity
import sports.vm.StepViewModel
import kotlin.getValue
import androidx.fragment.app.viewModels
import bind_device.BindStatusManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

@Route(path = "/cognitive/homeFragment")
class HomeFragment : Fragment() {

    lateinit var mIntent: Intent
    lateinit var btn_game: View
    lateinit var btn_speak: View
    lateinit var btn_schedule: View
    lateinit var btn_linked: View
    lateinit var tvSteps: TextView
    private val stepsViewModel: StepViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                mainViewModel.notifyRecordChanged()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_game = view.findViewById<Button>(R.id.game_layout)
        btn_speak = view.findViewById<Button>(R.id.speak_layout)
        btn_schedule = view.findViewById<Button>(R.id.schedule_layout)
        btn_linked = view.findViewById<TextView>(R.id.btn_linked)
        tvSteps = view.findViewById<TextView>(R.id.tv_steps)
        btn_speak.setOnClickListener {
            mIntent = Intent(requireContext(), RecordActivity::class.java)
            startActivity(mIntent) }
        btn_game.setOnClickListener {
            mIntent = Intent(requireContext(), SchulteGameActivity::class.java)
            startForResult.launch(mIntent)
        }
        btn_schedule.setOnClickListener {
            mIntent = Intent(requireContext(), ScheduleActivity::class.java)
            startForResult.launch(mIntent)
        }
        btn_linked.setOnClickListener {
            showCallOptionsDialog(this.requireContext())
        }
        stepsViewModel.stepCount.observe(viewLifecycleOwner) {
            tvSteps.text = "今日步数 $it"
        }
    }

    fun bindDevice() {
        if (BindStatusManager.isBound(requireContext())) {
            showCallOptionsDialog(requireContext())
        } else {

        }
    }

    fun showCallOptionsDialog(context: Context) {
        val dialog = BottomSheetDialog(context)
        dialog.setContentView(R.layout.dialog_bind)

        // 视频通话点击
        dialog.findViewById<TextView>(R.id.video_call)?.setOnClickListener {
            // 处理视频通话逻辑
            dialog.dismiss()
        }

        // 语音通话点击
        dialog.findViewById<TextView>(R.id.other_report)?.setOnClickListener {
            // 处理语音通话逻辑
            dialog.dismiss()
        }

        // 强制展开，避免默认半展开
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        stepsViewModel.refreshToday()
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
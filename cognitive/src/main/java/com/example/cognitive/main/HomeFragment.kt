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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import com.example.cognitive.R
import mine.ui.MineRecordFragment
import read_assessment.ui.RecordActivity
import schedule.ui.ScheduleActivity
import schulte.ui.SchulteGameActivity
import sports.vm.StepViewModel
import kotlin.getValue
import androidx.fragment.app.viewModels
import bind_device.BindActivity
import bind_device.BindRepository
import bind_device.BindRequest
import bind_device.BindStatusManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import other.OtherReportActivity

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
        mainViewModel.initTodaySaveYesterday()
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
            isBindDevice()
        }
        stepsViewModel.stepCount.observe(viewLifecycleOwner) {
            tvSteps.text = "今日步数 $it"
        }
    }

    fun isBindDevice() {
        if (BindStatusManager.isBound(requireContext())) {
            showCallOptionsDialog(requireContext())
        } else {
            bindDevice()
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

        // 查看报告点击
        dialog.findViewById<TextView>(R.id.other_report)?.setOnClickListener {
            val intent = Intent(requireContext(), OtherReportActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        // 强制展开，避免默认半展开
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()
    }

    fun bindDevice() {
        showBindDialog()
    }

    // 在 Fragment 中：用 requireContext() 获取上下文（避免空指针）
// 在 Activity 中：直接用 this 或 ActivityName.this
    fun showBindDialog() {
        AlertDialog.Builder(requireContext())
            // 1. 设置对话框标题
            .setTitle("绑定设备")
            // 2. 设置对话框内容
            .setMessage("暂无绑定设备，确定要执行这个操作吗？")
            // 3. 设置“取消”按钮
            .setNegativeButton("取消") { dialog, _ ->
                // 点击取消：仅关闭对话框（dialog.dismiss() 可省略，系统默认会关闭）
                dialog.dismiss()
            }
            // 4. 设置“确认”按钮
            .setPositiveButton("确认") { _, _ ->
                val intent = Intent(requireContext(), BindActivity::class.java)
                startActivity(intent)
            }
            // 5. 设置是否可通过点击外部/返回键关闭（可选）
            .setCancelable(true)
            // 6. 显示对话框
            .show()
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
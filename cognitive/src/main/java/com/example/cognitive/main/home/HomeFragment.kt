package com.example.cognitive.main.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import collection.HealthMonitoringFragment
import com.example.cognitive.R
import com.example.cognitive.main.MainViewModel
import com.example.common.bind_device.BindStatusManager
import com.example.common.login.remote.LoginStatusManager
import com.example.common.rtc.RtcActivity
import com.example.common.util.ItemSpacingDecoration
import game.BrainTrainingFragment
import read_assessment.ui.ReadFragment
import sports.data.StepForegroundService

class HomeFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()

    private val cameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions: Map<String, Boolean> ->
            var allGranted = true
            for (isGranted in permissions.values) {
                if (!isGranted) {
                    allGranted = false
                    break
                }
            }
            if (allGranted) {
                startStepService()
            } else {
                Toast.makeText(
                    requireContext(),
                    "通知权限未授予，可能无法正常接收步数提醒",
                    Toast.LENGTH_SHORT
                ).show()
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

        if (checkPermissions()) {
            startStepService()
        } else {
            cameraPermissionLauncher.launch(requiredPermissions)
        }

        val list: MutableList<HomeRVModel> = ArrayList()
        list.add(HomeRVModel(R.drawable.brain, "认知测试", R.color.blue))
        list.add(HomeRVModel(R.drawable.game, "大脑训练", R.color.green))
        list.add(HomeRVModel(R.drawable.pulse, "健康数据监测", R.color.blue))
        list.add(HomeRVModel(R.drawable.video, "视频通话", R.color.orange))

        val adapter = HomeRVAdapter(list)
        val recyclerView = view.findViewById<RecyclerView>(R.id.content)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener { position: Int ->
            when (position) {
                3 -> {
                    val userId = LoginStatusManager.getLoggedInUserId(requireContext())
                    val targetId = BindStatusManager.getBindStatus().second

                    if (userId.isEmpty() || targetId.isNullOrEmpty()) {
                        Log.e("HomeFragment", "用户ID或对方ID为空，无法发起RTC通话")
                        Toast.makeText(requireContext(), "请先登录并绑定设备", Toast.LENGTH_SHORT).show()
                        return@setOnItemClickListener
                    }

                    Log.d("HomeFragment", "发起RTC通话: userId=$userId, targetId=$targetId")
                    val intent = Intent(requireContext(), RtcActivity::class.java)
                    intent.putExtra("userId", userId)
                    intent.putExtra("targetId", targetId)
                    intent.putExtra("isElder", false)
                    startActivity(intent)
                }
                else -> {
                    val fragment = when (position) {
                        0 -> ReadFragment()
                        1 -> BrainTrainingFragment()
                        2 -> HealthMonitoringFragment()
                        else -> null
                    }

                    fragment?.let {
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            }
        }

        val itemSpacingDecoration = ItemSpacingDecoration(context, 24, false)
        recyclerView.addItemDecoration(itemSpacingDecoration)
    }

    private val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        } else {
            TODO("VERSION.SDK_INT < TIRAMISU")
        }

    private fun checkPermissions(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun startStepService() {
        val intent = Intent(requireContext(), StepForegroundService::class.java)
        requireContext().startForegroundService(intent)
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        fun newInstance(param1: String?, param2: String?): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
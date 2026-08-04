package com.example.cognitive.collection.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cognitive.collection.model.HealthMonitoringRVModel
import com.example.cognitive.R
import com.example.cognitive.databinding.FragmentHealthMonitoringBinding
import com.example.common.util.ItemSpacingDecoration
import kotlinx.coroutines.launch
import com.example.cognitive.schedule.ui.ScheduleActivity
import com.example.cognitive.schedule.vm.ScheduleViewModel
import com.example.cognitive.sports.vm.StepViewModel

class HealthMonitoringFragment : Fragment() {

    private var _binding: FragmentHealthMonitoringBinding? = null
    private val binding get() = _binding!!

    // 刷新回调接口
    interface OnRefreshListener {
        fun onRefresh()
    }

    private var refreshListener: OnRefreshListener? = null

    private lateinit var list: MutableList<HealthMonitoringRVModel?>
    private lateinit var adapter: HealthMonitoringRVAdapter

    fun setOnRefreshListener(listener: OnRefreshListener?) {
        this.refreshListener = listener
    }

    fun finishRefresh() {
        _binding?.swipeRefresh?.let { it.isRefreshing = false }
    }

    fun refreshData() {
        // 步数由 StepViewModel 的 DAO Flow 自动推送，无需手动查询
        scheduleViewModel.refreshBySystemEvents {
            _binding?.swipeRefresh?.let { it.isRefreshing = false }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private val stepsViewModel: StepViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthMonitoringBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list = ArrayList()
        list.add(HealthMonitoringRVModel("今日步数", 0.toDouble(), 10000.0, "steps"))
        list.add(HealthMonitoringRVModel("作息时间", 0.0, 10.0, "hours"))

        adapter = HealthMonitoringRVAdapter(list)

        binding.content.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    stepsViewModel.stepCount.collect { steps ->
                        list[0] = list[0]?.copy(data = steps)
                        adapter.notifyItemChanged(0)
                    }
                }
                launch {
                    scheduleViewModel.scheduleHours.collect { hours ->
                        list[1] = list[1]?.copy(data = hours)
                        adapter.notifyItemChanged(1)
                    }
                }
            }
        }

        binding.content.layoutManager = LinearLayoutManager(context)

        adapter.setOnItemClickListener { position: Int ->
            when (position) {
                1 -> {
                    startActivity(Intent(context, ScheduleActivity::class.java))
                }
            }
        }

        val itemSpacingDecoration = ItemSpacingDecoration(context, 20, false)
        binding.content.addItemDecoration(itemSpacingDecoration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
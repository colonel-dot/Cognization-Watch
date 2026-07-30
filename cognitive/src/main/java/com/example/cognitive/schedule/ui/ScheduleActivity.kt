package com.example.cognitive.schedule.ui

import android.app.AppOpsManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.schedule.vm.ScheduleViewModel

import com.example.cognitive.databinding.ActivityScheduleBinding
import com.example.cognitive.main.MainViewModel

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding

    private lateinit var bedHourAdapter: WheelAdapter
    private lateinit var bedMinuteAdapter: WheelAdapter
    private lateinit var wakeHourAdapter: WheelAdapter
    private lateinit var wakeMinuteAdapter: WheelAdapter

    private lateinit var map: HashMap<RecyclerView, LinearSnapHelper>

    private val viewModel: ScheduleViewModel by viewModels {
        ScheduleViewModel.Factory(application)
    }
    private val mainViewModel: MainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!checkUsageStatsPermission()) {
            Toast.makeText(this, "请开启使用情况访问权限", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        map = HashMap()
        map[binding.rvBedHour] = CircularSnapHelper(viewModel.hours.size)
        map[binding.rvBedMinute] = CircularSnapHelper(viewModel.minutes.size)
        map[binding.rvHour] = CircularSnapHelper(viewModel.hours.size)
        map[binding.rvMinute] = CircularSnapHelper(viewModel.minutes.size)

        bedHourAdapter = setupWheel(binding.rvBedHour, viewModel.hours, viewModel.bedHourPos) { selectedHour, pos ->
            viewModel.onBedTimeSelected(
                selectedHour,
                bedMinuteAdapter.getRealValue(),
                pos,
                bedMinuteAdapter.selectedPos
            )
        }

        bedMinuteAdapter = setupWheel(binding.rvBedMinute, viewModel.minutes, viewModel.bedMinutePos) { selectedMinute, pos ->
            viewModel.onBedTimeSelected(
                bedHourAdapter.getRealValue(),
                selectedMinute,
                bedHourAdapter.selectedPos,
                pos
            )
        }

        wakeHourAdapter = setupWheel(binding.rvHour, viewModel.hours, viewModel.wakeHourPos) { selectedHour, pos ->
            viewModel.onWakeTimeSelected(
                selectedHour,
                wakeMinuteAdapter.getRealValue(),
                pos,
                wakeMinuteAdapter.selectedPos
            )
        }

        wakeMinuteAdapter = setupWheel(binding.rvMinute, viewModel.minutes, viewModel.wakeMinutePos) { selectedMinute, pos ->
            viewModel.onWakeTimeSelected(
                wakeHourAdapter.getRealValue(),
                selectedMinute,
                wakeHourAdapter.selectedPos,
                pos
            )
        }

        binding.btnBed.setOnClickListener {
            viewModel.saveScheduleToDb(
                bedHour = bedHourAdapter.getRealValue(),
                bedMinute = bedMinuteAdapter.getRealValue(),
                wakeHour = wakeHourAdapter.getRealValue(),
                wakeMinute = wakeMinuteAdapter.getRealValue()
            )
            setResult(RESULT_OK)
            mainViewModel.notifyRecordChanged()
            Toast.makeText(this, "作息时间已保存", Toast.LENGTH_SHORT).show()
        }

        binding.btnRise.setOnClickListener {
            viewModel.saveScheduleToDb(
                bedHour = bedHourAdapter.getRealValue(),
                bedMinute = bedMinuteAdapter.getRealValue(),
                wakeHour = wakeHourAdapter.getRealValue(),
                wakeMinute = wakeMinuteAdapter.getRealValue()
            )
            setResult(RESULT_OK)
            mainViewModel.notifyRecordChanged()
            Toast.makeText(this, "作息时间已保存", Toast.LENGTH_SHORT).show()
        }

        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        if (checkUsageStatsPermission()) {
            viewModel.refreshBySystemEvents()
            setResult(RESULT_OK)
        }
    }

    private fun setupWheel(
        recyclerView: RecyclerView,
        data: List<String>,
        initialPos: Int,
        onItemSelected: (String, Int) -> Unit
    ): WheelAdapter {
        val adapter = WheelAdapter(data)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        recyclerView.onFlingListener = null
        map[recyclerView]?.attachToRecyclerView(recyclerView)

        val safePos = calculateSafePosition(initialPos, adapter)
        adapter.selectedPos = safePos

        recyclerView.post {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val itemHeight = 120
            val offset = recyclerView.height / 2 - itemHeight / 2
            layoutManager.scrollToPositionWithOffset(safePos, offset)
        }

        recyclerView.addOnScrollListener(createScrollListener(adapter, onItemSelected))
        return adapter
    }

    private fun calculateSafePosition(realPos: Int, adapter: WheelAdapter): Int {
        val middlePos = adapter.getMiddlePosition()
        val dataSize = adapter.origin.size
        val adjustedPos = (realPos % dataSize + dataSize) % dataSize
        return middlePos + adjustedPos
    }

    private fun createScrollListener(
        adapter: WheelAdapter,
        onItemSelected: (String, Int) -> Unit
    ): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
                    val snapView = map[rv]?.findSnapView(layoutManager) ?: return
                    val snapPos = rv.getChildAdapterPosition(snapView)

                    if (snapPos != RecyclerView.NO_POSITION) {
                        val realPos = getRealPosition(snapPos, adapter.origin.size)
                        adapter.selectedPos = snapPos
                        adapter.notifyDataSetChanged()
                        onItemSelected(adapter.origin[realPos], realPos)
                    }
                }
            }
        }
    }

    private fun getRealPosition(loopPosition: Int, dataSize: Int): Int {
        return (loopPosition % dataSize + dataSize) % dataSize
    }

    private fun setupObservers() {
        viewModel.bedTimeText.observe(this) { text ->
            binding.tvBedTime.text = text
            binding.rvBedHour.postDelayed({
                scrollWheelTo(viewModel.bedHourPos, bedHourAdapter, binding.rvBedHour)
                scrollWheelTo(viewModel.bedMinutePos, bedMinuteAdapter, binding.rvBedMinute)
            }, 50)
        }

        viewModel.wakeTimeText.observe(this) { text ->
            binding.tvWakeTime.text = text
            binding.rvHour.postDelayed({
                scrollWheelTo(viewModel.wakeHourPos, wakeHourAdapter, binding.rvHour)
                scrollWheelTo(viewModel.wakeMinutePos, wakeMinuteAdapter, binding.rvMinute)
            }, 50)
        }
    }

    private fun scrollWheelTo(pos: Int, adapter: WheelAdapter, rv: RecyclerView) {
        val safePos = calculateSafePosition(pos, adapter)
        rv.post {
            val layoutManager = rv.layoutManager as LinearLayoutManager
            val itemHeight = 120
            val offset = rv.height / 2 - itemHeight / 2

            layoutManager.scrollToPositionWithOffset(safePos, offset)

            adapter.selectedPos = safePos
            adapter.notifyDataSetChanged()
        }
    }

    private fun checkUsageStatsPermission(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
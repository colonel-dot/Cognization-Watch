package schedule.ui

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import schedule.vm.ScheduleViewModel

class ScheduleActivity : AppCompatActivity() {

    private lateinit var tvBedTime: TextView
    private lateinit var tvWakeTime: TextView

    private lateinit var bedHourAdapter: WheelAdapter
    private lateinit var bedMinuteAdapter: WheelAdapter
    private lateinit var wakeHourAdapter: WheelAdapter
    private lateinit var wakeMinuteAdapter: WheelAdapter

    private val viewModel: ScheduleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)
        checkUsageStatsPermission()
        // 初始化视图
        tvBedTime = findViewById(R.id.tvBedTime)
        tvWakeTime = findViewById(R.id.tvWakeTime)

        val rvBedHour = findViewById<RecyclerView>(R.id.rvBedHour)
        val rvBedMinute = findViewById<RecyclerView>(R.id.rvBedMinute)
        val rvWakeHour = findViewById<RecyclerView>(R.id.rvHour)
        val rvWakeMinute = findViewById<RecyclerView>(R.id.rvMinute)

        // 初始化睡觉时间滚轮
        bedHourAdapter = setupWheel(rvBedHour, viewModel.hours, viewModel.bedHourPos) { selectedHour, pos ->
            viewModel.onBedTimeSelected(selectedHour, bedMinuteAdapter.data[bedMinuteAdapter.selectedPos], pos, bedMinuteAdapter.selectedPos)
        }

        bedMinuteAdapter = setupWheel(rvBedMinute, viewModel.minutes, viewModel.bedMinutePos) { selectedMinute, pos ->
            viewModel.onBedTimeSelected(bedHourAdapter.data[bedHourAdapter.selectedPos], selectedMinute, bedHourAdapter.selectedPos, pos)
        }

        // 初始化起床时间滚轮
        wakeHourAdapter = setupWheel(rvWakeHour, viewModel.hours, viewModel.wakeHourPos) { selectedHour, pos ->
            viewModel.onWakeTimeSelected(selectedHour, wakeMinuteAdapter.data[wakeMinuteAdapter.selectedPos], pos, wakeMinuteAdapter.selectedPos)
        }

        wakeMinuteAdapter = setupWheel(rvWakeMinute, viewModel.minutes, viewModel.wakeMinutePos) { selectedMinute, pos ->
            viewModel.onWakeTimeSelected(wakeHourAdapter.data[wakeHourAdapter.selectedPos], selectedMinute, wakeHourAdapter.selectedPos, pos)
        }


        // 设置观察者
        setupObservers()
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

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        // 安全计算初始滚动位置
        val safePos = adapter.getMiddlePosition() + initialPos % data.size
        recyclerView.scrollToPosition(safePos)
        adapter.selectedPos = safePos

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snapView = snapHelper.findSnapView(rv.layoutManager) ?: return
                    val pos = rv.getChildAdapterPosition(snapView)
                    if (pos != RecyclerView.NO_POSITION) {
                        adapter.selectedPos = pos
                        adapter.notifyDataSetChanged()
                        // 取模防止越界
                        val realPos = pos % adapter.data.size
                        onItemSelected(adapter.data[realPos], realPos)
                    }
                }
            }
        })

        return adapter
    }


    private fun setupObservers() {
        viewModel.bedTimeText.observe(this, Observer { text ->
            tvBedTime.text = text
        })

        viewModel.wakeTimeText.observe(this, Observer { text ->
            tvWakeTime.text = text
        })
    }

    private fun checkUsageStatsPermission() {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        if (mode != AppOpsManager.MODE_ALLOWED) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            Toast.makeText(this, "请开启“使用情况访问权限”", Toast.LENGTH_LONG).show()
        }
    }

}

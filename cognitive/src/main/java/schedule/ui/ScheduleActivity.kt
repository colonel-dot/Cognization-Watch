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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import schedule.vm.ScheduleViewModel
import com.example.cognitive.R

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

        if (!checkUsageStatsPermission()) {
            Toast.makeText(this, "请开启“使用情况访问权限”", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        // 初始化视图
        tvBedTime = findViewById(R.id.tvBedTime)
        tvWakeTime = findViewById(R.id.tvWakeTime)

        val rvBedHour = findViewById<RecyclerView>(R.id.rvBedHour)
        val rvBedMinute = findViewById<RecyclerView>(R.id.rvBedMinute)
        val rvWakeHour = findViewById<RecyclerView>(R.id.rvHour)
        val rvWakeMinute = findViewById<RecyclerView>(R.id.rvMinute)

        // 初始化滚轮
        bedHourAdapter = setupWheel(rvBedHour, viewModel.hours, viewModel.bedHourPos) { selectedHour, pos ->
            viewModel.onBedTimeSelected(
                selectedHour,
                bedMinuteAdapter.getRealValue(),
                pos,
                bedMinuteAdapter.selectedPos
            )
        }

        bedMinuteAdapter = setupWheel(rvBedMinute, viewModel.minutes, viewModel.bedMinutePos) { selectedMinute, pos ->
            viewModel.onBedTimeSelected(
                bedHourAdapter.getRealValue(),
                selectedMinute,
                bedHourAdapter.selectedPos,
                pos
            )
        }

        wakeHourAdapter = setupWheel(rvWakeHour, viewModel.hours, viewModel.wakeHourPos) { selectedHour, pos ->
            viewModel.onWakeTimeSelected(
                selectedHour,
                wakeMinuteAdapter.getRealValue(),
                pos,
                wakeMinuteAdapter.selectedPos
            )
        }

        wakeMinuteAdapter = setupWheel(rvWakeMinute, viewModel.minutes, viewModel.wakeMinutePos) { selectedMinute, pos ->
            viewModel.onWakeTimeSelected(
                wakeHourAdapter.getRealValue(),
                selectedMinute,
                wakeHourAdapter.selectedPos,
                pos
            )
        }


        // 设置观察者（包括滚轮同步）
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        if (checkUsageStatsPermission()) {
            // 刷新系统亮屏/锁屏数据
            viewModel.refreshBySystemEvents()
        }
    }

    /** 初始化滚轮 */
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

        // 初始滚动位置
        //val safePos = adapter.getMiddlePosition() + initialPos % data.size
        val safePos = adapter.getMiddlePosition() + (initialPos % adapter.data.size + adapter.data.size) % adapter.data.size

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
                        val realPos = ((pos % adapter.data.size) + adapter.data.size) % adapter.data.size
                        onItemSelected(adapter.data[realPos], realPos)
                    }
                }
            }
        })

        return adapter
    }

    /** LiveData 观察者，同时滚轮滚动到选中位置 */
    private fun setupObservers() {
        viewModel.bedTimeText.observe(this) { text ->
            tvBedTime.text = text
            scrollWheelTo(viewModel.bedHourPos, bedHourAdapter, findViewById(R.id.rvBedHour))
            scrollWheelTo(viewModel.bedMinutePos, bedMinuteAdapter, findViewById(R.id.rvBedMinute))
        }

        viewModel.wakeTimeText.observe(this) { text ->
            tvWakeTime.text = text
            scrollWheelTo(viewModel.wakeHourPos, wakeHourAdapter, findViewById(R.id.rvHour))
            scrollWheelTo(viewModel.wakeMinutePos, wakeMinuteAdapter, findViewById(R.id.rvMinute))
        }
    }

    /** 滚动到指定位置 */
    private fun scrollWheelTo(pos: Int, adapter: WheelAdapter, rv: RecyclerView) {
        val safePos = adapter.getMiddlePosition() + (pos % adapter.data.size)
        rv.post {
            rv.scrollToPosition(safePos)
            adapter.selectedPos = safePos
            adapter.notifyDataSetChanged()
        }
    }

    /** 检查权限 */
    private fun checkUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}

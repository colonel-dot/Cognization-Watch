package schedule.ui

import android.app.AppOpsManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import schedule.vm.ScheduleViewModel
import com.example.cognitive.R
import com.example.cognitive.main.MainViewModel

class ScheduleActivity : AppCompatActivity() {

    private lateinit var tvBedTime: TextView
    private lateinit var tvWakeTime: TextView

    private lateinit var bedHourAdapter: WheelAdapter
    private lateinit var bedMinuteAdapter: WheelAdapter
    private lateinit var wakeHourAdapter: WheelAdapter
    private lateinit var wakeMinuteAdapter: WheelAdapter

    private lateinit var rvBedHour: RecyclerView
    private lateinit var rvBedMinute: RecyclerView
    private lateinit var rvWakeHour: RecyclerView
    private lateinit var rvWakeMinute: RecyclerView

    private lateinit var map: HashMap<RecyclerView, LinearSnapHelper>

    private lateinit var btn_rise: Button
    private lateinit var btn_bed: Button

    private val viewModel: ScheduleViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_schedule)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!checkUsageStatsPermission()) {
            Toast.makeText(this, "请开启使用情况访问权限", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        tvBedTime = findViewById(R.id.tvBedTime)
        tvWakeTime = findViewById(R.id.tvWakeTime)

        btn_bed = findViewById(R.id.btn_bed)
        btn_rise = findViewById(R.id.btn_rise)

        // 正确初始化RecyclerView[2,5](@ref)
        rvBedHour = findViewById(R.id.rvBedHour)
        rvBedMinute = findViewById(R.id.rvBedMinute)
        rvWakeHour = findViewById(R.id.rvHour)
        rvWakeMinute = findViewById(R.id.rvMinute)

        map = HashMap()
        map[rvBedHour] = CircularSnapHelper(viewModel.hours.size)
        map[rvBedMinute] = CircularSnapHelper(viewModel.minutes.size)
        map[rvWakeHour] = CircularSnapHelper(viewModel.hours.size)
        map[rvWakeMinute] = CircularSnapHelper(viewModel.minutes.size)

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

        btn_bed.setOnClickListener {
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

        btn_rise.setOnClickListener {
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

        // 设置观察者
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        if (checkUsageStatsPermission()) {
            // 不会重复刷新，因为 ViewModel 会拦住
            viewModel.refreshBySystemEvents()
            setResult(RESULT_OK)
        }
    }


    /** 修复后的初始化滚轮方法 */
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

        // 重点：使用 post 确保 rv.height 已知
        recyclerView.post {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val itemHeight = 120 // 确保这里和 Adapter onCreateViewHolder 里的高度一致
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

    // 创建滚动监听器
    private fun createScrollListener(
        adapter: WheelAdapter,
        onItemSelected: (String, Int) -> Unit
    ): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {

                // 滑动停止状态
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
                    val snapView = map[rv]?.findSnapView(layoutManager) ?: return
                    val snapPos = rv.getChildAdapterPosition(snapView)

                    if (snapPos != RecyclerView.NO_POSITION) {
                        // 准确计算真实位置
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
            tvBedTime.text = text
            // 使用正确的RecyclerView实例，添加延迟避免竞争条件
            rvBedHour.postDelayed({
                scrollWheelTo(viewModel.bedHourPos, bedHourAdapter, rvBedHour)
                scrollWheelTo(viewModel.bedMinutePos, bedMinuteAdapter, rvBedMinute)
            }, 50)
        }

        viewModel.wakeTimeText.observe(this) { text ->
            tvWakeTime.text = text
            rvWakeHour.postDelayed({
                scrollWheelTo(viewModel.wakeHourPos, wakeHourAdapter, rvWakeHour)
                scrollWheelTo(viewModel.wakeMinutePos, wakeMinuteAdapter, rvWakeMinute)
            }, 50)
        }
    }

    /** 修复后的滚动方法 */
    private fun scrollWheelTo(pos: Int, adapter: WheelAdapter, rv: RecyclerView) {
        val safePos = calculateSafePosition(pos, adapter)
        rv.post {
            val layoutManager = rv.layoutManager as LinearLayoutManager
            // 计算偏移量：(RecyclerView高度 / 2) - (Item高度 / 2)
            // 注意：Adapter里的TextView高度是120px，这里要保持一致
            val itemHeight = 120 // 如果你在Adapter里写的是120
            val offset = rv.height / 2 - itemHeight / 2

            layoutManager.scrollToPositionWithOffset(safePos, offset)

            adapter.selectedPos = safePos
            adapter.notifyDataSetChanged()
        }
    }

    /** 检查权限 */
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
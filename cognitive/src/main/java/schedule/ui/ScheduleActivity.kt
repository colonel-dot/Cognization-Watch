package schedule.ui

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.appcompat.app.AppCompatActivity
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

    private lateinit var btn_rise: Button
    private lateinit var btn_bed: Button

    private val viewModel: ScheduleViewModel by viewModels()

    // 使用单例SnapHelper避免重复绑定
    companion object {
        private val snapHelper by lazy { LinearSnapHelper() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        if (!checkUsageStatsPermission()) {
            Toast.makeText(this, "请开启使用情况访问权限", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        tvBedTime = findViewById(R.id.tvBedTime)
        tvWakeTime = findViewById(R.id.tvWakeTime)

        btn_bed = findViewById<Button>(R.id.btn_bed)
        btn_rise = findViewById<Button>(R.id.btn_rise)

        // 正确初始化RecyclerView[2,5](@ref)
        rvBedHour = findViewById(R.id.rvBedHour)
        rvBedMinute = findViewById(R.id.rvBedMinute)
        rvWakeHour = findViewById(R.id.rvHour)
        rvWakeMinute = findViewById(R.id.rvMinute)

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

            Toast.makeText(this, "作息时间已保存", Toast.LENGTH_SHORT).show()
        }


        btn_rise.setOnClickListener {
            viewModel.saveScheduleToDb(
                bedHour = bedHourAdapter.getRealValue(),
                bedMinute = bedMinuteAdapter.getRealValue(),
                wakeHour = wakeHourAdapter.getRealValue(),
                wakeMinute = wakeMinuteAdapter.getRealValue()
            )
            Toast.makeText(this, "作息时间已保存", Toast.LENGTH_SHORT).show()
        }


        // 设置观察者（包括滚轮同步）
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        if (checkUsageStatsPermission()) {
            // 不会重复刷新，因为 ViewModel 会拦住
            viewModel.refreshBySystemEvents()
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

        // 修复SnapHelper绑定问题
        recyclerView.onFlingListener = null // 先清除已有的fling listener
        snapHelper.attachToRecyclerView(recyclerView)

        // 统一的位置计算公式
        val safePos = calculateSafePosition(initialPos, adapter)
        recyclerView.scrollToPosition(safePos)
        adapter.selectedPos = safePos

        recyclerView.addOnScrollListener(createScrollListener(adapter, onItemSelected))
        return adapter
    }

    private fun calculateSafePosition(realPos: Int, adapter: WheelAdapter): Int {
        val middlePos = adapter.getMiddlePosition()
        val dataSize = adapter.origin.size
        val adjustedPos = (realPos % dataSize + dataSize) % dataSize
        return middlePos + adjustedPos
    }

    //创建滚动监听器
    private fun createScrollListener(
        adapter: WheelAdapter,
        onItemSelected: (String, Int) -> Unit
    ): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {

                //滑动停止状态
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
                    val snapView = snapHelper.findSnapView(layoutManager) ?: return
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

    /** 计算真实位置 */
    private fun getRealPosition(loopPosition: Int, dataSize: Int): Int {
        return (loopPosition % dataSize + dataSize) % dataSize
    }

    /** 修复后的LiveData观察者 - 使用正确的RecyclerView引用 */
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
            if (rv.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                rv.scrollToPosition(safePos)
                adapter.selectedPos = safePos
                adapter.notifyDataSetChanged()
            }
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
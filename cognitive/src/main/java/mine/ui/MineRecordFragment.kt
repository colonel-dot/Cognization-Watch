package mine.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import com.example.cognitive.main.MainViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.launch
import mine.vm.MineRecordViewModel
import com.example.common.persistense.AppDatabase
import com.example.common.persistense.behavior.DailyBehaviorEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "MineRecordFragment"
class MineRecordFragment :  Fragment(R.layout.fragment_mine_record){
    private val viewModel: MineRecordViewModel by viewModels<MineRecordViewModel>()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var recordRV: RecyclerView
    private lateinit var lineChart: LineChart
    // 声明适配器为全局变量，方便在监听中刷新数据
    private lateinit var recordAdapter: RecordRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recordRV = view.findViewById(R.id.record_rv)
        lineChart = view.findViewById<LineChart>(R.id.lineChart)
        initRecyclerView()
        initLineChart()
        lifecycleScope.launch {
            setChartData()
        }
        viewModel.queryRecordsData()
        observeViewModelData()
    }

    override fun onResume() {
        super.onResume()

    }

    private fun initRecyclerView(){
        recordRV.layoutManager = LinearLayoutManager(requireContext())
        recordAdapter = RecordRVAdapter(mutableListOf())
        recordRV.adapter = recordAdapter

        recordAdapter.setOnItemClickListener(object : RecordRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, record: DailyBehaviorEntity) {

                val date = record.date   // LocalDate

                val sheet = risk.ui.RiskDetailBottomSheet.newInstance(date)
                sheet.show(parentFragmentManager, "RiskDetailBottomSheet")
            }
        })
    }

    private fun initLineChart() {
        lineChart.apply {
            // 禁用描述文字
            description = Description().apply { text = "" }
            // 禁用缩放
            setScaleEnabled(false)
            // 禁用拖拽
            isDragEnabled = true
            // 禁用图例（单条折线无需图例）
            legend.isEnabled = false
            // 启用触摸反馈
            setTouchEnabled(true)
        }

        val yAxis = lineChart.axisLeft
        yAxis.apply {
            axisMinimum = 0.0f
            axisMaximum = 1.0f
            granularity = 0.1f
            setDrawGridLines(true)
        }
        lineChart.axisRight.isEnabled = false // 禁用右侧Y轴

        // 3. X轴配置（时间轴，显示15天日期）
        val xAxis = lineChart.xAxis
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM // X轴显示在底部
            setDrawGridLines(false)               // 隐藏X轴网格线
            granularity = 1f
            // 自定义X轴标签格式化（显示日期，如「03-01」）
            valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    // value是X轴索引（0=第1天，1=第2天...）
                    val dayIndex = value.toInt()
                    // 计算对应日期：从15天前开始
                    val date = LocalDate.now().plusDays(5).minusDays(14 - dayIndex.toLong())
                    return date.format(DateTimeFormatter.ofPattern("MM-dd"))
                }
            }
            axisMinimum = 0f
            axisMaximum = 14f
        }
    }

    private suspend fun setChartData() {
        val entries = ArrayList<Entry>()
        val riskDao = AppDatabase.getDatabase(this.requireContext()).dailyRiskDao()
        val riskRecords = riskDao.getAll()
        Log.d(TAG, "setChartData: $riskRecords")
        riskRecords.forEach { record ->
            val daysAgo = LocalDate.now().plusDays(5).until(record.date).days * -1 // 负数表示过去的天数
            val xIndex = 14 - daysAgo // 映射到0-14的X轴索引
            val yValue = record.riskScore ?: 0.0
            entries.add(Entry(xIndex.toFloat(), yValue.toFloat()))
        }

        val dataSet = LineDataSet(entries, "风险指数").apply {
            color = resources.getColor(R.color.blue, null) // 折线颜色
            circleRadius = 4f
            lineWidth = 2f
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = resources.getColor(R.color.red, null) // 填充颜色
            fillAlpha = 50 // 填充透明度
        }

        // 3. 设置数据到图表
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet)
        val lineData = LineData(dataSets)
        lineChart.data = lineData
        lineChart.invalidate() // 刷新图表
    }

    //  核心方法：监听ViewModel的LiveData，接收数据库返回的数据
    private fun observeViewModelData(){
        viewModel.allBehaviorData.observe(viewLifecycleOwner) { dataList ->
            // dataList 就是ViewModel从数据库查询到的所有DailyBehaviorEntity数据
            if (dataList.isNotEmpty()) {
                // 有数据：清空适配器原有数据，添加新数据，刷新列表
                recordAdapter.list.clear()
                recordAdapter.list.addAll(dataList)
                recordAdapter.notifyDataSetChanged()
            } else {
                // 无数据：清空列表，显示空页面
                recordAdapter.list.clear()
                recordAdapter.notifyDataSetChanged()
            }
        }
        mainViewModel.recordChanged.observe(viewLifecycleOwner) {
            viewModel.queryTodayRecordData()
        }
        viewModel.todayBehaviorData.observe(viewLifecycleOwner) { today ->
            if (today != null) {
                recordAdapter.updateItem(today)
            }
        }
    }
}
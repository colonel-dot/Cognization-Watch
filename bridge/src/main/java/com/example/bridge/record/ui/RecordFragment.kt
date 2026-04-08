package com.example.bridge.record.ui

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bridge.R
import com.example.common.login.remote.LoginStatusManager
import com.example.common.persistense.AppDatabase
import com.example.common.persistense.risk.DailyRiskEntity
import com.example.common.persistense.risk.RiskRepository
import com.example.common.repository.network.BindApiService
import com.example.common.repository.network.RetrofitClient
import com.example.common.util.ItemSpacingDecoration
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Collections
import java.util.TreeSet
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RecordFragment : Fragment() {

    private var week: TextView? = null
    private var half: TextView? = null
    private var lineChart: LineChart? = null
    private var record: RecyclerView? = null
    private var swipeRefresh: SwipeRefreshLayout? = null
    private var adapter: RecordRVAdapter? = null
    private var riskDataList: MutableList<DailyRiskEntity> = ArrayList()
    private var selectedDays = 15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // mParam1 = it.getString(ARG_PARAM1)
            // mParam2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("RecordFragment", "onViewCreated")

        try {
            bindView(view)
            bindClickListener()
            initLineChart()
            initRecyclerView()
        } catch (e: Exception) {
            Log.e("RecordFragment", "Error in onViewCreated", e)
            throw e
        }
    }

    private fun bindView(view: View) {
        week = view.findViewById(R.id.week)
        half = view.findViewById(R.id.half)
        record = view.findViewById(R.id.record)
        lineChart = view.findViewById(R.id.lineChart)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
    }

    private fun bindClickListener() {
        week?.setOnClickListener {
            selectedDays = 7
            updateButtonAppearance()
            loadRiskDataFromDatabase()
        }
        half?.setOnClickListener {
            selectedDays = 15
            updateButtonAppearance()
            loadRiskDataFromDatabase()
        }
        updateButtonAppearance()

        // 设置下拉刷新
        swipeRefresh?.setOnRefreshListener {
            // 从网络获取最新数据
            refreshFromNetwork()
        }
    }

    /**
     * 从网络获取绑定设备的风险数据并更新本地数据库和UI
     */
    private fun refreshFromNetwork() {
        val eldername = LoginStatusManager.getLoggedInUserId(requireContext())
        if (eldername.isNullOrEmpty()) {
            Log.w("RecordFragment", "未登录，无法获取绑定设备数据")
            swipeRefresh?.isRefreshing = false
            return
        }

        // 停止使用显式的 ExecutorService，改用协程作用域
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. 获取 Service
                val apiService = RetrofitClient.createService(BindApiService::class.java)

                // 2. 调用 suspend 函数 (现在可以正常调用了)
                val networkData = apiService.getAllDailyRisk(eldername)

                if (!networkData.isNullOrEmpty()) {
                    Log.d("RecordFragment", "从网络获取到 ${networkData.size} 条风险数据")

                    // 3. 获取 DAO 并更新本地数据库
                    val database = AppDatabase.getDatabase(requireContext())
                    database.dailyRiskDao().insertAll(networkData)
                    Log.d("RecordFragment", "已更新本地数据库")

                    // 4. 切换到主线程更新 UI
                    withContext(Dispatchers.Main) {
                        loadRiskDataFromDatabase()
                        swipeRefresh?.isRefreshing = false
                    }
                } else {
                    Log.d("RecordFragment", "网络数据为空")
                    withContext(Dispatchers.Main) {
                        swipeRefresh?.isRefreshing = false
                    }
                }
            } catch (e: Exception) {
                Log.e("RecordFragment", "从网络获取数据失败", e)
                withContext(Dispatchers.Main) {
                    swipeRefresh?.isRefreshing = false
                    // 可以给用户一个错误提示
                    Toast.makeText(context, "同步失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateButtonAppearance() {
        if (selectedDays == 7) {
            week?.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
            half?.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.deep_grey)
        } else {
            week?.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.deep_grey)
            half?.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue)
        }
    }

    private fun initLineChart() {
        lineChart?.description?.isEnabled = false
        lineChart?.legend?.isEnabled = false
        lineChart?.setTouchEnabled(true)
        lineChart?.isDragEnabled = true
        lineChart?.setScaleEnabled(false)
        lineChart?.setDrawGridBackground(false)

        val tf = Typeface.createFromAsset(requireContext().assets, "fonts/harmonyos_sans_regular.ttf")

        val xAxis: XAxis = lineChart!!.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.typeface = tf
        xAxis.granularity = 1f

        val yAxis: YAxis = lineChart!!.axisLeft
        yAxis.setDrawGridLines(true)
        yAxis.typeface = tf
        yAxis.granularity = 0.1f
        lineChart?.axisRight?.isEnabled = false

        updateLineChartData(riskDataList)
    }

    private fun initRecyclerView() {
        adapter = RecordRVAdapter(ArrayList())
        record?.layoutManager = LinearLayoutManager(context)
        record?.adapter = adapter

        val itemSpacingDecoration = ItemSpacingDecoration(requireContext(), 3, 16, false)
        record?.addItemDecoration(itemSpacingDecoration)

        adapter?.setOnRecordClickListener { position, riskEntity ->
            val fm: FragmentManager? = parentFragmentManager
            if (fm?.findFragmentByTag("RiskDetailBottomSheet") != null) {
                return@setOnRecordClickListener
            }

            val date: LocalDate = riskEntity.date
            val sheet = RecordDetailBottomSheet.newInstance(date)
            sheet.show(parentFragmentManager, "RiskDetailBottomSheet")
        }

        loadRiskDataFromDatabase()
    }

    private fun loadRiskDataFromDatabase() {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val database = AppDatabase.getDatabase(requireContext())
                val fromDate: LocalDate = LocalDate.now().minusDays((selectedDays - 1).toLong())
                val list = RiskRepository.getFromBlocking(database.dailyRiskDao(), fromDate)

                // Reverse to show latest first
                val result: MutableList<DailyRiskEntity> = ArrayList(list)
                Collections.reverse(result)

                requireActivity().runOnUiThread {
                    riskDataList = result
                    adapter?.setList(result)
                    updateLineChartData(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        executor.shutdown()
    }

    private fun updateLineChartData(list: List<DailyRiskEntity>?) {
        val entries: MutableList<Entry> = ArrayList()
        if (list != null && !list.isEmpty()) {
            for (i in 1 until list.size) {
                val entity = list[i]
                entries.add(Entry(i.toFloat(), entity.riskScore.toFloat()))
            }
        }
        val dataSet = LineDataSet(entries, "")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.blue)
        dataSet.lineWidth = 1f
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)

        lineChart?.data = LineData(dataSet)

        // Update Y axis range and labels
        val yLabels = TreeSet<Float>()
        for (entry in entries) {
            val value = entry.y
            val floor = (Math.floor((value * 10).toDouble()) / 10.0).toFloat()
            val ceil = (Math.ceil((value * 10).toDouble()) / 10.0).toFloat()
            yLabels.add(floor)
            yLabels.add(ceil)
        }
        val yAxis: YAxis? = lineChart?.axisLeft
        if (yLabels.isNotEmpty()) {
            yAxis?.axisMinimum = yLabels.first()
            yAxis?.axisMaximum = yLabels.last()
            yAxis?.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.1f", value)
                }
            }
        } else {
            // Set default range if no data
            yAxis?.axisMinimum = 0f
            yAxis?.axisMaximum = 1f
        }
        yAxis?.isGranularityEnabled = true

        lineChart?.invalidate()
    }

    companion object {
        fun newInstance(param1: String, param2: String): RecordFragment {
            val fragment = RecordFragment()
            val args = Bundle()
            args.putString("param1", param1)
            args.putString("param2", param2)
            fragment.arguments = args
            return fragment
        }
    }
}

package com.example.bridge.record.ui

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bridge.R
import com.example.common.login.GuestStateHolder
import com.example.common.login.remote.LoginStatusManager
import com.example.common.persistense.AppDatabase
import com.example.common.persistense.risk.DailyRiskEntity
import com.example.common.persistense.risk.RiskRepository
import com.example.common.repository.network.BindApiService
import com.example.common.repository.network.RetrofitClient
import com.example.common.record.ui.RecordDetailBottomSheet
import com.example.common.util.ItemSpacingDecoration
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.TreeSet
import kotlin.math.ceil
import kotlin.math.floor

class RecordFragment : Fragment() {

    private var week: TextView? = null
    private var half: TextView? = null
    private var lineChart: LineChart? = null
    private var record: RecyclerView? = null
    private var swipeRefresh: SwipeRefreshLayout? = null
    private var adapter: RecordRVAdapter? = null
    private var riskDataList: MutableList<DailyRiskEntity> = ArrayList()
    private var selectedDays = 15

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
            loadRecordDataWithNetworkFirst()
        }
        half?.setOnClickListener {
            selectedDays = 15
            updateButtonAppearance()
            loadRecordDataWithNetworkFirst()
        }
        updateButtonAppearance()

        swipeRefresh?.setOnRefreshListener {
            refreshFromNetwork()
        }
    }

    private fun refreshFromNetwork() {
        if (GuestStateHolder.isGuest()) {
            Log.d("RecordFragment", "游客模式，直接刷新本地数据")
            loadRiskDataFromDatabase()
            swipeRefresh?.isRefreshing = false
            return
        }

        val eldername = LoginStatusManager.getLoggedInUserId(requireContext())
        if (eldername.isEmpty()) {
            Log.w("RecordFragment", "未登录，无法获取绑定设备数据")
            swipeRefresh?.isRefreshing = false
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val apiService = RetrofitClient.createService(BindApiService::class.java)
                val networkData = apiService.getAllDailyRisk(eldername)

                if (networkData.isNotEmpty()) {
                    Log.d("RecordFragment", "从网络获取到 ${networkData.size} 条风险数据")
                    val database = AppDatabase.getDatabase(requireContext())
                    database.dailyRiskDao().insertAll(networkData)
                    Log.d("RecordFragment", "已更新本地数据库")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "同步成功", Toast.LENGTH_SHORT).show()
                        loadRiskDataFromDatabase()
                        swipeRefresh?.isRefreshing = false
                    }
                } else {
                    Log.d("RecordFragment", "网络数据为空")
                    withContext(Dispatchers.Main) {
                        // TODO：暂时注释
                        // Toast.makeText(context, "暂无数据", Toast.LENGTH_SHORT).show()
                        swipeRefresh?.isRefreshing = false
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("RecordFragment", "从网络获取数据失败", e)
                withContext(Dispatchers.Main) {
                    swipeRefresh?.isRefreshing = false
                    Toast.makeText(context, "同步失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadRecordDataWithNetworkFirst() {
        viewLifecycleOwner.lifecycleScope.launch {
            swipeRefresh?.isRefreshing = true
            try {
                val (data, networkSuccess) = withContext(Dispatchers.IO) {
                    loadRecordData()
                }
                if (view == null) return@launch
                updateUI(data)
                if (networkSuccess) {
                    Toast.makeText(requireContext(), "同步成功", Toast.LENGTH_SHORT).show()
                } else {
                    // TODO：暂时注释
                    // Toast.makeText(requireContext(), "暂无数据", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("RecordFragment", "加载数据失败: ${e.message}", e)
                Toast.makeText(requireContext(), "刷新失败", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefresh?.isRefreshing = false
            }
        }
    }

    private suspend fun loadRecordData(): Pair<List<DailyRiskEntity>, Boolean> = withContext(Dispatchers.IO) {
        val eldername = LoginStatusManager.getLoggedInUserId(requireContext())
        if (eldername.isEmpty()) {
            return@withContext Pair(emptyList(), false)
        }

        // 游客模式直接读本地，不走网络
        if (GuestStateHolder.isGuest()) {
            Log.d("RecordFragment", "游客模式，直接读取本地数据")
            val database = AppDatabase.getDatabase(requireContext())
            val fromDate: LocalDate = LocalDate.now().minusDays((selectedDays - 1).toLong())
            val list = RiskRepository.getFromBlocking(database.dailyRiskDao(), fromDate)
            val result: MutableList<DailyRiskEntity> = ArrayList(list)
            result.reverse()
            return@withContext Pair(result, false)
        }

        // 网络接口拉取和存库
        var networkSuccess = false
        try {
            val apiService = RetrofitClient.createService(BindApiService::class.java)
            val networkData = apiService.getAllDailyRisk(eldername)
            if (networkData.isNotEmpty()) {
                val database = AppDatabase.getDatabase(requireContext())
                database.dailyRiskDao().insertAll(networkData)
                Log.d("RecordFragment", "网络数据已存库: ${networkData.size} 条")
                networkSuccess = true
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("RecordFragment", "网络拉取失败，读取本地缓存: ${e.message}")
        }

        // 读库
        val database = AppDatabase.getDatabase(requireContext())
        val fromDate: LocalDate = LocalDate.now().minusDays((selectedDays - 1).toLong())
        val list = RiskRepository.getFromBlocking(database.dailyRiskDao(), fromDate)
        val result: MutableList<DailyRiskEntity> = ArrayList(list)
        result.reverse()
        Pair(result, networkSuccess)
    }

    private fun updateUI(list: List<DailyRiskEntity>) {
        riskDataList = list.toMutableList()
        adapter?.setList(list)
        updateLineChartData(list)
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

        adapter?.setOnRecordClickListener { _, riskEntity ->
            val fm: FragmentManager = parentFragmentManager
            if (fm.findFragmentByTag("RecordDetailBottomSheet") != null) {
                return@setOnRecordClickListener
            }

            val date: LocalDate = riskEntity.date
            val sheet = RecordDetailBottomSheet.newInstance(date)
            sheet.show(parentFragmentManager, "RecordDetailBottomSheet")
        }

        loadRecordDataWithNetworkFirst()
    }

    private fun loadRiskDataFromDatabase() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(requireContext())
                val fromDate: LocalDate = LocalDate.now().minusDays((selectedDays - 1).toLong())
                val list = RiskRepository.getFromBlocking(database.dailyRiskDao(), fromDate)

                val result: MutableList<DailyRiskEntity> = ArrayList(list)
                result.reverse()

                withContext(Dispatchers.Main) {
                    riskDataList = result
                    adapter?.setList(result)
                    updateLineChartData(result)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
            }
        }
    }

    private fun updateLineChartData(list: List<DailyRiskEntity>?) {
        val entries: MutableList<Entry> = ArrayList()
        var x = 0
        if (list != null && !list.isEmpty()) {
            for (i in list.size - 1 downTo  0) {
                val entity = list[i]
                entries.add(Entry((++x).toFloat(), entity.riskScore.toFloat()))
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
            val floor = (floor((value * 10).toDouble()) / 10.0).toFloat()
            val ceil = (ceil((value * 10).toDouble()) / 10.0).toFloat()
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
            yAxis?.axisMinimum = 0f
            yAxis?.axisMaximum = 1f
        }
        yAxis?.isGranularityEnabled = true

        lineChart?.invalidate()
    }
}

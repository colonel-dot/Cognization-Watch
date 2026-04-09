package com.example.bridge.dashboard.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bridge.R
import com.example.bridge.dashboard.item.DashboardAlertItem
import com.example.bridge.dashboard.item.DashboardCollectionItem
import com.example.bridge.dashboard.item.DashboardRiskItem
import com.example.bridge.dashboard.item.DashboardRtcItem
import com.example.bridge.main.ChildrenActivity
import com.example.common.bind_device.BindStatusManager
import com.example.common.login.remote.LoginStatusManager
import com.example.common.persistense.AppDatabase
import com.example.common.persistense.behavior.DailyBehaviorDao
import com.example.common.persistense.behavior.DailyBehaviorEntity
import com.example.common.persistense.risk.DailyRiskDao
import com.example.common.persistense.risk.DailyRiskEntity
import com.example.common.repository.network.DashboardNetworkRepository
import com.example.common.rtc.RtcActivity
import com.example.common.util.ItemSpacingDecoration
import com.example.common.util.OnItemClickListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class DashboardFragment : Fragment() {

    private val TAG = "DashboardFragment"

    private var recyclerView: RecyclerView? = null
    private var swipeRefresh: SwipeRefreshLayout? = null
    private var adapter: DashboardRVAdapter? = null

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.content)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)

        adapter = DashboardRVAdapter()
        adapter!!.list = ArrayList()

        // 初始化占位数据
        adapter!!.list.add(DashboardRtcItem("没有数据源"))
        adapter!!.list.add(DashboardRiskItem(0.0, 0.0))
        adapter!!.list.add(DashboardCollectionItem(0, 0.0))
        adapter!!.list.add(DashboardAlertItem("没有数据源"))

        adapter!!.setOnRtcClickListener(OnItemClickListener { position ->
            val userId = LoginStatusManager.getLoggedInUserId(requireContext())
            val targetId = BindStatusManager.getBindStatus().second

            if (userId.isNullOrEmpty() || targetId.isNullOrEmpty()) {
                Log.e(TAG, "用户ID或对方ID为空，无法发起RTC通话")
                Toast.makeText(requireContext(), "请先登录并绑定设备", Toast.LENGTH_SHORT).show()
                return@OnItemClickListener
            }

            Log.d(TAG, "发起RTC通话: userId=$userId, targetId=$targetId")
            val intent = Intent(requireContext(), RtcActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("targetId", targetId)
            intent.putExtra("isElder", false)
            startActivity(intent)
        })

        adapter!!.setOnAlertClickListener(OnItemClickListener { position ->
            if (activity is ChildrenActivity) {
                (activity as ChildrenActivity).switchToGeofenceFragment()
            }
        })

        recyclerView!!.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView!!.adapter = adapter
        recyclerView!!.addItemDecoration(ItemSpacingDecoration(requireContext(), 20, false))

        swipeRefresh!!.setOnRefreshListener {
            loadDashboardDataWithRefreshComplete()
        }

        loadDashboardDataWithRefreshComplete()
    }

    private fun loadDashboardDataWithRefreshComplete() {
        lifecycleScope.launch {
            try {
                val (data, hasData) = withContext(Dispatchers.IO) {
                    loadDashboardData()
                }
                if (view == null) return@launch
                updateDashboardUI(data)
                if (hasData) {
                    Toast.makeText(requireContext(), "刷新成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "暂无数据", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "加载仪表盘数据失败: ${e.message}", e)
                Toast.makeText(requireContext(), "刷新失败", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefresh?.isRefreshing = false
            }
        }
    }

    private data class DashboardLoadResult(val data: DashboardData, val hasData: Boolean)

    private suspend fun loadDashboardData(): DashboardLoadResult = coroutineScope {
        val username = LoginStatusManager.getLoggedInUserId(requireContext())
        val childAccount = username
        val elderAccount = BindStatusManager.getBindStatus().second

        if (childAccount.isNullOrEmpty() || elderAccount.isNullOrEmpty()) {
            return@coroutineScope DashboardLoadResult(
                DashboardData(username ?: "", 0.0, 0.0, 0, 0.0, "请先绑定设备"),
                hasData = false
            )
        }

        val database = AppDatabase.getDatabase(requireContext())
        val riskDao: DailyRiskDao = database.dailyRiskDao()
        val behaviorDao: DailyBehaviorDao = database.dailyBehaviorDao()

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        // 并行请求今日风险、昨日风险、行为数据
        val todayRiskDeferred = async { fetchTodayRisk(childAccount, elderAccount, today, riskDao) }
        val yesterdayRiskDeferred = async { fetchYesterdayRisk(childAccount, elderAccount, yesterday, riskDao) }
        val behaviorDeferred = async { fetchBehavior(childAccount, elderAccount, today, behaviorDao) }

        val todayRisk = todayRiskDeferred.await()
        val yesterdayRisk = yesterdayRiskDeferred.await()
        val behavior = behaviorDeferred.await()

        val todayRiskScore = todayRisk?.riskScore ?: 0.0
        val yesterdayRiskScore = yesterdayRisk?.riskScore ?: 0.0
        val todaySteps = behavior?.steps ?: 0
        val todaySleepHours = (behavior?.sleepMinute ?: 0) / 60.0

        val hasData = todayRisk != null || behavior != null
        DashboardLoadResult(
            DashboardData(username, todayRiskScore, yesterdayRiskScore, todaySteps, todaySleepHours, "没有数据源"),
            hasData
        )
    }

    private suspend fun fetchTodayRisk(childAccount: String, elderAccount: String, date: LocalDate, riskDao: DailyRiskDao): DailyRiskEntity? {
        return try {
            var result: DailyRiskEntity? = null
            DashboardNetworkRepository.getElderDailyRisk(childAccount, elderAccount, date).collect { riskResult ->
                result = riskResult.getOrNull()
            }
            result?.also { riskDao.insert(it) }
            result
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "获取今日风险数据失败: ${e.message}")
            null
        }
    }

    private suspend fun fetchYesterdayRisk(childAccount: String, elderAccount: String, date: LocalDate, riskDao: DailyRiskDao): DailyRiskEntity? {
        return try {
            var result: DailyRiskEntity? = null
            DashboardNetworkRepository.getElderDailyRisk(childAccount, elderAccount, date).collect { riskResult ->
                result = riskResult.getOrNull()
            }
            result?.also { riskDao.insert(it) }
            result
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "获取昨日风险数据失败: ${e.message}")
            null
        }
    }

    private suspend fun fetchBehavior(childAccount: String, elderAccount: String, date: LocalDate, behaviorDao: DailyBehaviorDao): DailyBehaviorEntity? {
        return try {
            var result: DailyBehaviorEntity? = null
            DashboardNetworkRepository.getElderDailyBehavior(childAccount, elderAccount, date).collect { behaviorResult ->
                result = behaviorResult.getOrNull()
            }
            result?.also { behaviorDao.insert(it) }
            result
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "获取行为数据失败: ${e.message}")
            null
        }
    }

    private fun updateDashboardUI(data: DashboardData) {
        Log.d(TAG, "$TAG.updateDashboardUI")
        if (adapter!!.list.size > 0) {
            adapter!!.list[0] = DashboardRtcItem(data.username)
        }
        if (adapter!!.list.size > 1) {
            adapter!!.list[1] = DashboardRiskItem(data.todayRiskScore, data.yesterdayRiskScore)
        }
        if (adapter!!.list.size > 2) {
            adapter!!.list[2] = DashboardCollectionItem(data.todaySteps, data.todaySleepHours)
        }
        if (adapter!!.list.size > 3) {
            adapter!!.list[3] = DashboardAlertItem(data.alertTip)
        }
        adapter!!.notifyDataSetChanged()
    }

    data class DashboardData(
        val username: String,
        val todayRiskScore: Double,
        val yesterdayRiskScore: Double,
        val todaySteps: Int,
        val todaySleepHours: Double,
        val alertTip: String
    )

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): DashboardFragment {
            return DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        }
    }
}

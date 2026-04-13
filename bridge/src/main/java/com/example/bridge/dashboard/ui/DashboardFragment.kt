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
import com.example.common.login.GuestStateHolder
import com.example.common.login.remote.LoginStatusManager
import com.example.common.persistense.AppDatabase
import com.example.common.persistense.geofence.GeofenceItem
import com.example.common.persistense.geofence.GeofenceRepository
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

    companion object {
        private const val TAG = "DashboardFragment"
    }

    private var recyclerView: RecyclerView? = null
    private var swipeRefresh: SwipeRefreshLayout? = null
    private var adapter: DashboardRVAdapter? = null

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

        adapter!!.setOnRtcClickListener(OnItemClickListener { _ ->
            val userId = LoginStatusManager.getLoggedInUserId(requireContext())
            val targetId = BindStatusManager.getBindStatus().second

            if (userId.isEmpty() || targetId.isNullOrEmpty()) {
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

        adapter!!.setOnAlertClickListener { _ ->
            if (activity is ChildrenActivity) {
                (activity as ChildrenActivity).switchToGeofenceFragment()
            }
        }

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
            swipeRefresh?.isRefreshing = true
            try {
                val (data, hasData) = withContext(Dispatchers.IO) {
                    loadDashboardData()
                }
                if (view == null) return@launch
                updateDashboardUI(data)
                if (hasData) {
                    Toast.makeText(requireContext(), "刷新成功", Toast.LENGTH_SHORT).show()
                } else {
                    // TODO：暂时注释
                    // Toast.makeText(requireContext(), "暂无数据", Toast.LENGTH_SHORT).show()
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

        if (childAccount.isEmpty() || elderAccount.isNullOrEmpty()) {
            return@coroutineScope DashboardLoadResult(
                DashboardData(username, 0.0, 0.0, 0, 0.0, "请先绑定设备"),
                hasData = false
            )
        }

        val database = AppDatabase.getDatabase(requireContext())
        val riskDao: DailyRiskDao = database.dailyRiskDao()
        val behaviorDao: DailyBehaviorDao = database.dailyBehaviorDao()

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val isGuest = GuestStateHolder.isGuest()

        // 并行请求今日风险、昨日风险和行为数据
        val todayRiskDeferred = async {
            if (isGuest) {
                val result = withContext(Dispatchers.IO) { riskDao.getByDate(today) }
                Log.d(TAG, "guest todayRisk query result: $result")
                result
            } else {
                fetchTodayRisk(childAccount, elderAccount, today, riskDao)
            }
        }
        val yesterdayRiskDeferred = async {
            if (isGuest) {
                val result = withContext(Dispatchers.IO) { riskDao.getByDate(yesterday) }
                Log.d(TAG, "guest yesterdayRisk query result: $result")
                result
            } else {
                fetchYesterdayRisk(childAccount, elderAccount, yesterday, riskDao)
            }
        }
        val behaviorDeferred = async {
            if (isGuest) {
                val result = withContext(Dispatchers.IO) { behaviorDao.getByDate(today) }
                Log.d(TAG, "guest behavior query result: steps=${result?.steps}, sleepMinute=${result?.sleepMinute}")
                result
            } else {
                fetchBehavior(childAccount, elderAccount, today, behaviorDao)
            }
        }

        val todayRisk = todayRiskDeferred.await()
        val yesterdayRisk = yesterdayRiskDeferred.await()
        val behavior = behaviorDeferred.await()

        val todayRiskScore = todayRisk?.riskScore ?: 0.0
        val yesterdayRiskScore = yesterdayRisk?.riskScore ?: 0.0
        val todaySteps = behavior?.steps ?: 0
        val sleepMins = behavior?.sleepMinute ?: 0
        val wakeMins = behavior?.wakeMinute ?: 0
        val sleepDurationMins = if (wakeMins >= sleepMins) {
            wakeMins - sleepMins
        } else {
            (24 * 60 - sleepMins) + wakeMins
        }
        val todaySleepHours = sleepDurationMins / 60.0

        // 获取最新围栏事件
        val latestGeofence = withContext(Dispatchers.IO) {
            GeofenceRepository.getLatestEvent(requireContext())
        }
        val alertTip = latestGeofence?.let { getGeofenceTip(it) } ?: "没有数据源"

        Log.d(TAG, "loadDashboardData: todayRisk=${todayRisk?.riskScore}, behavior.steps=${behavior?.steps}, latestGeofence=$latestGeofence")

        val hasData = todayRisk != null || behavior != null || latestGeofence != null
        DashboardLoadResult(
            DashboardData(username, todayRiskScore, yesterdayRiskScore, todaySteps, todaySleepHours, alertTip),
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
        if (adapter!!.list.isNotEmpty()) {
            val boundUsername = BindStatusManager.getBindStatus().second
            val remark = BindStatusManager.getBindRemark(requireContext())
            val displayName = remark ?: boundUsername ?: data.username
            adapter!!.list[0] = DashboardRtcItem(displayName)
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

    private fun getGeofenceTip(item: GeofenceItem): String {
        return when (item.status) {
            GeofenceItem.STATUS_IN -> "已进入围栏"
            GeofenceItem.STATUS_OUT -> "已离开围栏"
            GeofenceItem.STATUS_STAYED -> "已停留10分钟"
            GeofenceItem.STATUS_LOCAL -> "定位失败"
            else -> "未知状态"
        }
    }
}

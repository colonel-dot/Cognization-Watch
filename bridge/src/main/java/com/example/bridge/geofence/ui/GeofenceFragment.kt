package com.example.bridge.geofence.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapWrapper
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.example.bridge.R
import com.example.bridge.geofence.feature.GeofenceStatusManager
import com.example.common.geofence.model.BarrierInfo
import com.example.bridge.geofence.vm.GeoViewModel
import com.example.bridge.geofence.map.bridge.MAWebViewWrapper
import com.example.bridge.geofence.map.view.MapWebView
import com.example.bridge.geofence.vm.FenceUiState
import com.example.bridge.geofence.vm.MovementUiState
import com.example.common.persistense.geofence.GeofenceItem
import com.example.common.persistense.geofence.GeofenceRepository
import com.example.common.login.remote.LoginStatusManager
import com.example.common.util.ItemSpacingDecoration
import com.example.common.util.StringMap
import kotlinx.coroutines.launch
import com.example.common.geofence.model.ElderMovement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

class GeofenceFragment : Fragment() {

    private lateinit var viewModel: GeoViewModel

    private var location: TextView? = null
    private var swipeRefresh: SwipeRefreshLayout? = null
    private var record: RecyclerView? = null
    private var adapter: GeofenceRVAdapter? = null

    private var map: MapWebView? = null
    private var aMapWrapper: AMapWrapper? = null
    private var aMap: AMap? = null

    private var itemMarker: Marker? = null

    // 定时轮询老人轨迹
    private var pollTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_geofence, container, false)
    }

    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("GeofenceFragment", "onViewCreated")

        GeofenceRepository.initialize(requireContext())

        bindView(view)

        initMAWebViewWrapper()

        initRecyclerView()

        initViewModel()

        observeUiState()

        // 如果未保存围栏信息，弹出设置对话框
        if (!GeofenceStatusManager.isFenceEnabled(requireContext())) {
            showGeofenceDialog()
        }

        // 启动轮询老人轨迹
        startPollTimer()
    }

    override fun onResume() {
        super.onResume()
        if (pollTimer == null) {
            startPollTimer()
        }
    }

    override fun onPause() {
        super.onPause()
        stopPollTimer()
    }

    private fun showGeofenceDialog() {
        val dialog = GeofenceDialogFragment()
        dialog.setOnFenceCreatedListener { lat, lng, radius ->
            Log.d("GeofenceFragment", "围栏参数获取成功: lat=$lat, lng=$lng, radius=$radius")
            // 发送到绑定设备
            sendBarrierInfoToRemote(lat, lng, radius)
        }
        dialog.show(childFragmentManager, "GeofenceDialog")
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(GeoViewModel::class.java)
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fenceUiState.collect { state ->
                when (state) {
                    is FenceUiState.PostSuccess -> {
                        Log.d("GeofenceFragment", "围栏信息发送到远端成功")
                    }
                    is FenceUiState.Error -> {
                        Log.e("GeofenceFragment", "围栏信息发送到远端失败: ${state.msg}")
                    }
                    else -> {}
                }
            }
        }

        // 观察老人轨迹数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movementUiState.collect { state ->
                when (state) {
                    is MovementUiState.GetSuccess -> {
                        Log.d("GeofenceFragment", "收到老人轨迹数据: ${state.data}")
                        handleElderMovement(state.data)
                    }
                    is MovementUiState.Error -> {
                        Log.e("GeofenceFragment", "获取老人轨迹失败: ${state.msg}")
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 将围栏信息发送到远端服务器
     */
    private fun sendBarrierInfoToRemote(lat: Double, lng: Double, radius: Float) {
        val eldername = LoginStatusManager.getLoggedInUserId(requireContext())
        val barrierInfo = BarrierInfo(eldername, lng, lat, radius.toDouble())
        viewModel.postBarrierInfo(eldername, barrierInfo)
    }

    /**
     * 处理老人轨迹数据（将事件存入本地数据库并刷新UI）
     */
    private suspend fun handleElderMovement(movement: ElderMovement) {
        val localStatus = when (movement.status?: "") {
            "IN" -> GeofenceItem.STATUS_IN
            "OUT" -> GeofenceItem.STATUS_OUT
            "STAYED" -> GeofenceItem.STATUS_STAYED
            else -> GeofenceItem.STATUS_UNKNOWN
        }

        val minutes = (movement.time / 1000 / 60).toInt()
        val item = GeofenceItem(
            id = 0,
            timestamp = minutes,
            lat = movement.lat,
            lng = movement.lon,
            status = localStatus
        )

        GeofenceRepository.insertEventBlocking(item)
        Log.d("GeofenceFragment", "老人轨迹事件已存入本地: status=${movement.status}, lat=${movement.lat}, lng=${movement.lon}")

        withContext(Dispatchers.Main) {
            // 只有 UI 刷新才切回主线程
            refreshData()
        }
    }

    /**
     * 启动定时轮询老人轨迹
     */
    private fun startPollTimer() {
        pollTimer?.cancel()
        pollTimer = Timer()
        pollTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                pollElderMovement()
            }
        }, 0, 30000) // 每30秒轮询一次
        Log.d("GeofenceFragment", "开始轮询老人轨迹")
    }

    /**
     * 轮询老人轨迹数据
     */
    private fun pollElderMovement() {
        val eldername = LoginStatusManager.getLoggedInUserId(requireContext())
        Log.d("GeofenceFragment", "轮询老人轨迹: eldername=$eldername")
        viewModel.getElderMovement(eldername)
    }

    /**
     * 停止轮询
     */
    private fun stopPollTimer() {
        pollTimer?.cancel()
        pollTimer = null
        Log.d("GeofenceFragment", "停止轮询老人轨迹")
    }

    private fun bindView(view: View) {
        location = view.findViewById(R.id.location)
        map = view.findViewById(R.id.map)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        record = view.findViewById(R.id.record)
    }

    private fun initMAWebViewWrapper() {
        val webViewWrapper = MAWebViewWrapper(map!!)
        aMapWrapper = AMapWrapper(requireContext(), webViewWrapper)
        aMapWrapper!!.onCreate()
        aMapWrapper!!.getMapAsyn { map ->
            aMap = map

            val items = GeofenceRepository.getAllEventsBlocking()
            if (aMap != null && items.isNotEmpty()) {
                val item = items[0]
                val latLng = LatLng(item.lat, item.lng)
                if (itemMarker != null) itemMarker!!.remove()
                itemMarker = aMap!!.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(StringMap.mapMinuteToRelativeTime(item.timestamp))
                        .draggable(false)
                        .visible(true)
                )
                aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            }
        }
    }

    private fun initRecyclerView() {
        record!!.layoutManager = LinearLayoutManager(context)
        adapter = GeofenceRVAdapter(ArrayList())
        record!!.adapter = adapter

        val itemSpacingDecoration = ItemSpacingDecoration(requireContext(), 6, 20, 6, false)
        record!!.addItemDecoration(itemSpacingDecoration)

        swipeRefresh!!.setOnRefreshListener { refreshData() }
        swipeRefresh!!.setColorSchemeResources(android.R.color.holo_blue_light)

        refreshData()
    }

    private fun refreshData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val items = GeofenceRepository.getAllEventsBlocking()
            adapter!!.list = items

            adapter!!.setOnItemClickListener { position ->
                val item = items[position]
                val latLng = LatLng(item.lat, item.lng)
                if (itemMarker != null) itemMarker!!.remove()
                itemMarker = aMap!!.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(StringMap.mapMinuteToRelativeTime(item.timestamp))
                        .draggable(false)
                        .visible(true)
                )
                aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            }

            adapter!!.notifyDataSetChanged()
            swipeRefresh!!.isRefreshing = false
        }
    }

    private fun insertSampleDataIfEmpty() {
        val existing = GeofenceRepository.getAllEventsBlocking()
        if (existing.isEmpty()) {
            val minutesSince1970 = (System.currentTimeMillis() / 1000 / 60).toInt()

            val sampleItems = ArrayList<GeofenceItem>()
            sampleItems.add(GeofenceItem(0, minutesSince1970 - 60, 34.261111, 108.942222, GeofenceItem.STATUS_IN))
            sampleItems.add(GeofenceItem(0, minutesSince1970 - 30, 34.262050, 108.943100, GeofenceItem.STATUS_OUT))
            sampleItems.add(GeofenceItem(0, minutesSince1970 - 15, 34.261600, 108.941700, GeofenceItem.STATUS_STAYED))
            sampleItems.add(GeofenceItem(0, minutesSince1970 - 5, 34.260800, 108.942800, GeofenceItem.STATUS_IN))

            for (item in sampleItems) {
                GeofenceRepository.insertEventBlocking(item)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPollTimer()
        aMapWrapper?.onDestroy()
    }

    companion object {
        fun newInstance(param1: String, param2: String): GeofenceFragment {
            val fragment = GeofenceFragment()
            val args = Bundle()
            args.putString("param1", param1)
            args.putString("param2", param2)
            fragment.arguments = args
            return fragment
        }
    }
}

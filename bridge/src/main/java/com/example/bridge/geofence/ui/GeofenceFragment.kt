package com.example.bridge.geofence.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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
import com.example.common.login.GuestStateHolder
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

    private var pollTimer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_geofence, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("GeofenceFragment", "onViewCreated")

        GeofenceRepository.initialize(requireContext())

        bindView(view)

        initMAWebViewWrapper()

        initRecyclerView()

        initViewModel()

        observeUiState()

        if (!GeofenceStatusManager.isFenceEnabled(requireContext())) {
            showGeofenceDialog()
        }

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
            sendBarrierInfoToRemote(lat, lng, radius)
        }
        dialog.show(childFragmentManager, "GeofenceDialog")
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[GeoViewModel::class.java]
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

    private fun sendBarrierInfoToRemote(lat: Double, lng: Double, radius: Float) {
        if (GuestStateHolder.isGuest()) {
            Log.d("GeofenceFragment", "游客模式，跳过围栏信息上传")
            return
        }
        val eldername = LoginStatusManager.getLoggedInUserId(requireContext())
        val barrierInfo = BarrierInfo(eldername, lng, lat, radius.toDouble())
        viewModel.postBarrierInfo(eldername, barrierInfo)
    }

    private suspend fun handleElderMovement(movement: ElderMovement) {
        val localStatus = when (movement.status) {
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
            refreshData()
        }
    }

    private fun startPollTimer() {
        pollTimer?.cancel()
        pollTimer = Timer()
        pollTimer?.schedule(object : TimerTask() {
            override fun run() {
                pollElderMovement()
            }
        }, 0, 30000)
        Log.d("GeofenceFragment", "开始轮询老人轨迹")
    }

    private fun pollElderMovement() {
        if (GuestStateHolder.isGuest()) {
            Log.d("GeofenceFragment", "游客模式，跳过轨迹轮询")
            return
        }
        val eldername = LoginStatusManager.getLoggedInUserId(requireContext())
        Log.d("GeofenceFragment", "轮询老人轨迹: eldername=$eldername")
        viewModel.getElderMovement(eldername)
    }

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

    private fun updateLocationStatus(item: GeofenceItem) {
        val (text, colorRes) = when (item.status) {
            GeofenceItem.STATUS_IN -> "围栏内" to R.color.green
            GeofenceItem.STATUS_OUT -> "围栏外" to R.color.red
            GeofenceItem.STATUS_STAYED -> "围栏驻留" to R.color.orange
            GeofenceItem.STATUS_LOCAL -> "定位失败" to R.color.deep_grey
            else -> "未知" to R.color.grey
        }
        location?.text = text
        location?.backgroundTintList = ContextCompat.getColorStateList(requireContext(), colorRes)
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
                updateLocationStatus(item)
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
                updateLocationStatus(item)
            }

            adapter!!.notifyDataSetChanged()
            swipeRefresh!!.isRefreshing = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPollTimer()
        aMapWrapper?.onDestroy()
    }
}

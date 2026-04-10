package collection.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import collection.model.HealthMonitoringRVModel
import com.example.cognitive.R
import com.example.common.persistense.AppDatabase
import com.example.common.util.ItemSpacingDecoration
import kotlinx.coroutines.launch
import schedule.ui.ScheduleActivity
import schedule.vm.ScheduleViewModel
import sports.vm.StepViewModel
import java.time.LocalDate

class HealthMonitoringFragment : Fragment() {

    // 刷新回调接口
    interface OnRefreshListener {
        fun onRefresh()
    }

    private var refreshListener: OnRefreshListener? = null

    private lateinit var list: MutableList<HealthMonitoringRVModel?>
    private lateinit var adapter: HealthMonitoringRVAdapter

    fun setOnRefreshListener(listener: OnRefreshListener?) {
        this.refreshListener = listener
    }

    fun finishRefresh() {
        view?.findViewById<SwipeRefreshLayout?>(R.id.swipeRefresh)?.isRefreshing = false
    }

    fun refreshData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val dao = AppDatabase.Companion.getDatabase(requireContext()).dailyBehaviorDao()
                val entity = dao.getOrInitTodayBehavior(LocalDate.now())
                val steps = entity.steps?.toDouble() ?: 0.0

                if (list.isNotEmpty() && list[0] != null) {
                    list[0] = list[0]!!.copy(data = steps)
                    adapter.notifyItemChanged(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        scheduleViewModel.refreshBySystemEvents {
            view?.findViewById<SwipeRefreshLayout?>(R.id.swipeRefresh)?.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private val stepsViewModel: StepViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_health_monitoring, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView?>(R.id.content)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout?>(R.id.swipeRefresh)

        list = ArrayList()
        list.add(HealthMonitoringRVModel("今日步数", 0.toDouble(), 10000.0, "steps"))
        list.add(HealthMonitoringRVModel("作息时间", 0.0, 10.0, "hours"))

        adapter = HealthMonitoringRVAdapter(list)

        recyclerView?.adapter = adapter

        swipeRefresh?.setOnRefreshListener {
            refreshData()
        }

        stepsViewModel.stepCount.observe(viewLifecycleOwner) { steps ->
            list[0] = list[0]?.copy(data = steps)
            adapter.notifyItemChanged(0)
        }

        scheduleViewModel.scheduleHours.observe(viewLifecycleOwner) { hours ->
            list[1] = list[1]?.copy(data = hours ?: 0.0)
            adapter.notifyItemChanged(1)
        }

        recyclerView?.setLayoutManager(LinearLayoutManager(context))

        adapter.setOnItemClickListener { position: Int ->
            when (position) {
                1 -> {
                    startActivity(Intent(context, ScheduleActivity::class.java))
                }
            }
        }

        val itemSpacingDecoration = ItemSpacingDecoration(context, 20, false)
        recyclerView?.addItemDecoration(itemSpacingDecoration)
    }
}
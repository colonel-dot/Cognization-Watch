package collection

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
import com.example.cognitive.R
import com.example.common.persistense.AppDatabase
import schedule.ui.ScheduleActivity
import schedule.vm.ScheduleViewModel
import sports.vm.StepViewModel
import com.example.common.util.ItemSpacingDecoration
import com.example.common.util.OnItemClickListener
import kotlinx.coroutines.launch

class HealthMonitoringFragment : Fragment() {

    // 刷新回调接口，供外部实现
    interface OnRefreshListener {
        fun onRefresh()
    }

    private var refreshListener: OnRefreshListener? = null

    // 将 list 和 adapter 提升为成员变量，以便 refreshData() 访问
    private lateinit var list: MutableList<HealthMonitoringRVModel?>
    private lateinit var adapter: HealthMonitoringRVAdapter

    fun setOnRefreshListener(listener: OnRefreshListener?) {
        this.refreshListener = listener
    }

    // 结束刷新状态，供外部调用
    fun finishRefresh() {
        view?.findViewById<SwipeRefreshLayout?>(R.id.swipeRefresh)?.isRefreshing = false
    }

    // 刷新数据源，供外部或 onResume 调用
    fun refreshData() {
        // 步数：直接从数据库读取并更新 UI
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val dao = AppDatabase.getDatabase(requireContext()).dailyBehaviorDao()
                val entity = dao.getOrInitTodayBehavior(java.time.LocalDate.now())
                val steps = entity.steps?.toDouble() ?: 0.0

                // 直接更新 list 并刷新 adapter
                if (list.size > 0 && list[0] != null) {
                    list[0] = list[0]!!.copy(data = steps)
                    adapter.notifyItemChanged(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 作息时间：重新从数据库/系统读取，等数据加载完成后再结束刷新状态
        scheduleViewModel.refreshBySystemEvents {
            view?.findViewById<SwipeRefreshLayout?>(R.id.swipeRefresh)?.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        // 从其他页面返回时自动刷新数据
        refreshData()
    }

    private var mParam1: String? = null
    private var mParam2: String? = null

    private val stepsViewModel: StepViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getArguments() != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
    }

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

        list = ArrayList<HealthMonitoringRVModel?>()
        list.add(HealthMonitoringRVModel("今日步数", 0.toDouble(), 10000.0, "steps"))
        list.add(HealthMonitoringRVModel("作息时间", 0.0, 10.0, "hours"))

        adapter = HealthMonitoringRVAdapter(list)

        recyclerView.adapter = adapter

        // 设置下拉刷新
        swipeRefresh?.setOnRefreshListener {
            // 下拉刷新时也调用 refreshData()
            // 刷新状态会在 scheduleViewModel 数据加载完成后自动结束
            refreshData()
        }

        // 观察步数数据
        stepsViewModel.stepCount.observe(viewLifecycleOwner) { steps ->
            list[0] = list[0]?.copy(data = steps)
            adapter.notifyItemChanged(0)
        }

        // 观察作息时间数据 - 从 ScheduleViewModel 读取
        scheduleViewModel.scheduleHours.observe(viewLifecycleOwner) { hours ->
            list[1] = list[1]?.copy(data = hours ?: 0.0)
            adapter.notifyItemChanged(1)
        }

        recyclerView.setLayoutManager(LinearLayoutManager(getContext()))

        adapter.setOnItemClickListener(OnItemClickListener { position: Int ->
            when (position) {
                1 -> {
                    startActivity(Intent(getContext(), ScheduleActivity::class.java))
                }
            }
        })

        val itemSpacingDecoration = ItemSpacingDecoration(getContext(), 20, false)
        recyclerView.addItemDecoration(itemSpacingDecoration)
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        fun newInstance(param1: String?, param2: String?): HealthMonitoringFragment {
            val fragment = HealthMonitoringFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.setArguments(args)
            return fragment
        }
    }
}

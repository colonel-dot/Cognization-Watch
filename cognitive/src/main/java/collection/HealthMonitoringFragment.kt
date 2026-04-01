package collection

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import schedule.ui.ScheduleActivity
import schedule.vm.ScheduleViewModel
import sports.vm.StepViewModel
import com.example.common.util.ItemSpacingDecoration
import com.example.common.util.OnItemClickListener

class HealthMonitoringFragment : Fragment() {
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

        val list: MutableList<HealthMonitoringRVModel?> = ArrayList<HealthMonitoringRVModel?>()
        val recyclerView = view.findViewById<RecyclerView?>(R.id.content)

        list.add(HealthMonitoringRVModel("今日步数", 0.toDouble(), 10000.0, "steps"))
        list.add(HealthMonitoringRVModel("作息时间", 0.0, 10.0, "hours"))

        val adapter = HealthMonitoringRVAdapter(list)

        recyclerView.adapter = adapter

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

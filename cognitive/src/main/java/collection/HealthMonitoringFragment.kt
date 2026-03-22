package collection

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import com.example.cognitive.main.ConMainActivity
import schedule.ui.ScheduleActivity
import sports.vm.StepViewModel
import util.ItemSpacingDecoration
import util.OnItemClickListener
import kotlin.getValue

private const val TAG = "HealthMonitoringFragmen"

class HealthMonitoringFragment : Fragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null

    private val stepsViewModel: StepViewModel by viewModels()


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
        Log.d(TAG, "onViewCreated:这里接收到的步数是${stepsViewModel.stepCount.value}")
        val list: MutableList<HealthMonitoringRVModel?> = ArrayList<HealthMonitoringRVModel?>()
        val recyclerView = view.findViewById<RecyclerView?>(R.id.content)


        list.add(HealthMonitoringRVModel("今日步数", 0.toDouble(), 10000.0, "steps"))
        list.add(HealthMonitoringRVModel("作息时间", 7.2, 10.0, "hours"))
        list.add(HealthMonitoringRVModel("静息心率", 64.0, 80.0, "bpm/min"))
        list.add(HealthMonitoringRVModel("血压", 71.0, 90.0, "mmHg"))

        val adapter = HealthMonitoringRVAdapter(list)

        recyclerView.adapter = adapter
        stepsViewModel.stepCount.observe(viewLifecycleOwner) { steps ->
            list[0] = list[0]?.copy(data = steps)
            adapter.notifyItemChanged(0)
        }

        recyclerView.setLayoutManager(LinearLayoutManager(getContext()))

        adapter.setOnItemClickListener(OnItemClickListener { position: Int ->
            when (position) {
                1 -> {
                    startActivity(Intent (getContext(), ScheduleActivity::class.java))
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
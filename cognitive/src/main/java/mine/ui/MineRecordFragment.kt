package mine.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import mine.vm.MineRecordViewModel
import persistense.DailyBehaviorEntity

class MineRecordFragment :  Fragment(R.layout.fragment_mine_record){
    // 你的写法正确：by viewModels() 是 ktx扩展语法，等价于ViewModelProvider(this)，更简洁
    private val viewModel: MineRecordViewModel by viewModels<MineRecordViewModel>()
    private lateinit var recordRV: RecyclerView
    // 声明适配器为全局变量，方便在监听中刷新数据
    private lateinit var recordAdapter: RecordRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 1. 绑定RecyclerView控件
        recordRV = view.findViewById(R.id.record_rv)
        // 2. 初始化RecyclerView【必须2行】：设置布局管理器 + 初始化适配器
        initRecyclerView()
        // 3. 监听ViewModel的数据变化【核心】：接收数据库数据，刷新列表
        observeViewModelData()
        // 4. 触发ViewModel查询数据库数据
        viewModel.queryRecordsData()
    }

    //  初始化RecyclerView 【RecyclerView必须的配置，缺一不可】
    private fun initRecyclerView(){
        // ① 设置布局管理器：竖向列表，固定写法，RecyclerView没这个就不会显示
        recordRV.layoutManager = LinearLayoutManager(requireContext())
        // ② 初始化适配器，传入空集合占位
        recordAdapter = RecordRVAdapter(mutableListOf())
        // ③ 给RecyclerView绑定适配器
        recordRV.adapter = recordAdapter

        recordAdapter.setOnItemClickListener(object : RecordRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, record: DailyBehaviorEntity) {

                val date = record.date   // LocalDate

                val sheet = risk.ui.RiskDetailBottomSheet.newInstance(date)
                sheet.show(parentFragmentManager, "RiskDetailBottomSheet")
            }
        })

    }

    //  核心方法：监听ViewModel的LiveData，接收数据库返回的数据
    private fun observeViewModelData(){
        viewModel.allBehaviorData.observe(viewLifecycleOwner) { dataList ->
            // dataList 就是ViewModel从数据库查询到的所有DailyBehaviorEntity数据
            if (dataList.isNotEmpty()) {
                // 有数据：清空适配器原有数据，添加新数据，刷新列表
                recordAdapter.list.clear()
                recordAdapter.list.addAll(dataList)
                recordAdapter.notifyDataSetChanged()
            } else {
                // 无数据：清空列表，显示空页面（可选，根据你的需求加）
                recordAdapter.list.clear()
                recordAdapter.notifyDataSetChanged()
            }
        }
    }
}
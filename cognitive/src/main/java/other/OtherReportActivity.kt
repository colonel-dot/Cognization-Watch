package other

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import kotlinx.coroutines.launch
import mine.ui.RecordRVAdapter
import mine.vm.MineRecordViewModel
import persistense.DailyBehaviorEntity
import kotlin.getValue

class OtherReportActivity : AppCompatActivity() {

    private val viewModel: OtherReportViewModel by viewModels<OtherReportViewModel>()


    private lateinit var recordRV: RecyclerView
    // 声明适配器为全局变量，方便在监听中刷新数据
    private lateinit var recordAdapter: RecordRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_other_report)
        recordRV = findViewById<RecyclerView>(R.id.other_record_rv)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initRecyclerView()
        viewModel.getOtherDailyBehavior()

        //observeViewModelData()
    }

    private fun initRecyclerView(){
        recordRV.layoutManager = LinearLayoutManager(this)
        recordAdapter = RecordRVAdapter(mutableListOf())
        recordRV.adapter = recordAdapter

        recordAdapter.setOnItemClickListener(object : RecordRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, record: DailyBehaviorEntity) {

                val date = record.date   // LocalDate

                val sheet = risk.ui.RiskDetailBottomSheet.newInstance(date)
                sheet.show(supportFragmentManager, "RiskDetailBottomSheet")
            }
        })
    }

    private fun observeRequestStatus(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.operationResult.collect { result ->
                    when (result) {
                        is OtherReportResult.BehaviorLoadSuccess -> {
                            Toast.makeText(this@OtherReportActivity, "行为数据加载成功", Toast.LENGTH_SHORT).show()
                        }
                        is OtherReportResult.RiskLoadSuccess -> {
                            Toast.makeText(this@OtherReportActivity, "风险数据加载成功", Toast.LENGTH_SHORT).show()
                        }
                        is OtherReportResult.LoadFailure -> {
                            Toast.makeText(this@OtherReportActivity, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    /*private fun observeViewModelData(){
        viewModel.allBehaviorData.observe(this) { dataList ->
            // dataList 就是ViewModel从数据库查询到的所有DailyBehaviorEntity数据
            if (dataList.isNotEmpty()) {
                // 有数据：清空适配器原有数据，添加新数据，刷新列表
                recordAdapter.list.clear()
                recordAdapter.list.addAll(dataList)
                recordAdapter.notifyDataSetChanged()
            } else {
                // 无数据：清空列表，显示空页面
                recordAdapter.list.clear()
                recordAdapter.notifyDataSetChanged()
            }
        }

        viewModel.todayBehaviorData.observe(this) { today ->
            if (today != null) {
                recordAdapter.updateItem(today)
            }
        }
    }*/

}
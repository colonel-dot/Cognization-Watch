package other

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import kotlinx.coroutines.launch
import mine.ui.RecordRVAdapter
import persistense.behavior.DailyBehaviorEntity

class OtherReportActivity : AppCompatActivity() {

    private val viewModel: OtherReportViewModel by viewModels()

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

        observeRequestStatus()

        observeDailyBehaviorData()

        viewModel.getOtherDailyBehavior()
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

    // 监听请求结果（成功/失败的Toast提示）
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

    // 新增：监听数据库中的行为数据，更新RecyclerView
    private fun observeDailyBehaviorData() {
        lifecycleScope.launch {
            // 仅在页面处于RESUMED状态时监听，避免后台消耗
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                // 收集ViewModel暴露的数据库数据Flow
                viewModel.dailyBehaviorList.collect { behaviorList ->
                    // 更新适配器数据
                    recordAdapter.list.clear()
                    recordAdapter.list.addAll(behaviorList)
                    // 通知适配器刷新UI
                    recordAdapter.notifyDataSetChanged()

                    // 可选：如果数据为空，显示空页面提示
                    if (behaviorList.isEmpty()) {
                        Toast.makeText(this@OtherReportActivity, "暂无行为数据", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}
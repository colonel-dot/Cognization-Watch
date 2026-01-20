package mine.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import mine.vm.MineRecordViewModel
import persistense.DailyBehaviorEntity

class MineRecordActivity : AppCompatActivity() {
    // 你的写法正确：by viewModels() 是 ktx扩展语法，等价于ViewModelProvider(this)，更简洁
    private val viewModel: MineRecordViewModel by viewModels()
    private lateinit var recordRV: RecyclerView
    // 声明适配器为全局变量，方便在监听中刷新数据
    private lateinit var recordAdapter: RecordRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mine_record)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 1. 绑定RecyclerView控件
        recordRV = findViewById(R.id.record_rv)
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
        recordRV.layoutManager = LinearLayoutManager(this)
        // ② 初始化适配器，传入空集合占位
        recordAdapter = RecordRVAdapter(mutableListOf())
        // ③ 给RecyclerView绑定适配器
        recordRV.adapter = recordAdapter
        // 【可选】绑定Adapter的条目点击事件
        /*recordAdapter.setOnItemClickListener { position, record ->

        }*/
    }

    //  核心方法：监听ViewModel的LiveData，接收数据库返回的数据
    private fun observeViewModelData(){
        viewModel.allBehaviorData.observe(this) { dataList ->
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
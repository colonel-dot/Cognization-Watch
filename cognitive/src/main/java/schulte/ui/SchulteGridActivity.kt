package schulte.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import com.example.cognitive.main.MainViewModel
import schulte.data.SchulteEvaluatorType
import schulte.data.SchulteGridCell
import schulte.engine.SchulteGridEngine
import risk.work.RiskConfigManager
import schulte.vm.SchulteGameViewModel
import com.example.common.util.TimerHelper
import kotlin.getValue

class SchulteGridActivity : AppCompatActivity() {

    private var time: TextView? = null
    private var grid: TextView? = null
    private var schulte: RecyclerView? = null
    private var pause: TextView? = null
    private var start: TextView? = null

    private var timer: TimerHelper? = null
    private var engine: SchulteGridEngine? = null
    private var adapter: SchulteGridRVAdapter? = null
    private val viewModel: SchulteGameViewModel by viewModels<SchulteGameViewModel>()
    private val mainViewModel: MainViewModel by viewModels<MainViewModel>()
    private var ms = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_schulte_grid)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 绑定视图
        bindView()

        // 初始化计时器
        initTimer()

        // 初始化游戏引擎和RecyclerView
        initGameEngine()

        // 绑定点击事件
        bindClickListener()

        grid?.text = engine!!.cur.toString() + " / " + engine!!.end
    }

    private fun bindView() {
        time = findViewById(R.id.time)
        grid = findViewById(R.id.grid)
        schulte = findViewById(R.id.schulte)
        pause = findViewById(R.id.pause)
        start = findViewById(R.id.start)
    }

    private fun initTimer() {
        timer = TimerHelper()
        timer!!.setOnTimerListener { `val`: Long ->
            ms = `val`
            val second = Math.toIntExact(`val` / 1000)
            time!!.text = second.toString()
        }
    }

    /**
     * 初始化游戏引擎和RecyclerView
     */
    private fun initGameEngine() {
        val type = RiskConfigManager(this).getSchulteEvaluatorType()
        Log.d("SchulteGridActivity", "读取到类型: ${type.name}, isFourSquared=${type == SchulteEvaluatorType.GRID_4}")
        engine = SchulteGridEngine(type)

        // 初始化单元格数据
        val list = ArrayList<SchulteGridCell?>()
        for (i in 1..engine!!.end) {
            list.add(SchulteGridCell(i))
        }
        // 设置RecyclerView适配器
        adapter = SchulteGridRVAdapter(list)
        schulte!!.setAdapter(adapter)

        adapter!!.setOnItemClickListener { pos: Int ->
            val res = engine!!.click(adapter!!.list[pos].num)
            when (res) {
                -1 -> { // 点击错误
                }
                1 -> { // 游戏完成
                    timer!!.stop()
                    engine!!.stop()
                    start!!.text = "开始"
                    pause!!.text = "暂停"
                    showFinishedDialog()
                    Log.d(TAG, "initGameEngine: 要保存并更新舒尔特成绩了")
                    viewModel.saveGameTime(if (engine!!.isFourSquared) 4 else 5, ms)
                    mainViewModel.notifyRecordChanged()
                }
                0 -> { // 点击正确
                    grid!!.text = engine!!.cur.toString() + " / " + engine!!.end
                }
            }
        }

        val span = if (engine!!.isFourSquared) 4 else 5
        schulte!!.setLayoutManager(GridLayoutManager(this, span))
    }

    /**
     * 绑定按钮点击事件
     */
    private fun bindClickListener() {
        // 暂停/继续按钮
        pause!!.setOnClickListener { _: View? ->
            if (engine!!.state == SchulteGridEngine.State.RUNNING) {
                timer!!.pause()
                engine!!.pause()
                pause!!.text = "继续"
            } else if (engine!!.state == SchulteGridEngine.State.PAUSED) {
                timer!!.resume()
                engine!!.resume()
                pause!!.text = "暂停"
            }
        }

        // 开始/结束按钮
        start!!.setOnClickListener { _: View? ->
            if (engine!!.state == SchulteGridEngine.State.STOPPED) {
                timer!!.start()
                engine!!.start()
                adapter!!.shuffle()
                start!!.text = "结束"
                pause!!.text = "暂停"
            } else {
                // 重置游戏状态
                timer!!.stop()
                engine!!.stop()
                start!!.text = "开始"
                pause!!.text = "暂停"

                time!!.text = "0"
                grid!!.text = "0 / " + engine!!.end

                // 重置单元格选中状态
                for (cell in adapter!!.list) {
                    cell.isSelected = false
                }
                adapter!!.notifyDataSetChanged()
            }
        }
    }

    /**
     * 游戏完成弹窗
     */
    private fun showFinishedDialog() {
        val seconds = ms / 1000.0

        AlertDialog.Builder(this) // Activity中直接用this替代requireContext()
            .setTitle("完成！")
            .setMessage(String.format("用时 %.3f 秒", seconds))
            .setPositiveButton(
                "确定"
            ) { dialog: DialogInterface?, _: Int ->
                // 重置显示
                time!!.text = "0"
                grid!!.text = "0 / " + engine!!.end
                dialog!!.dismiss()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (timer != null) {
            timer!!.stop()
        }
        if (engine != null) {
            engine!!.stop()
        }
    }

    companion object {
        private const val TAG = "SchulteGridActivity"
    }
}
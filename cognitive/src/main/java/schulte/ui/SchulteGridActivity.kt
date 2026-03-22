package schulte.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import com.example.cognitive.main.MainViewModel
import mine.vm.MineRecordViewModel
import schulte.data.SchulteGridCell
import schulte.engine.SchulteGridEngine
import schulte.vm.SchulteGameViewModel
import util.OnItemClickListener
import util.OnTimerListener
import util.TimerHelper
import kotlin.getValue

class SchulteGridActivity : AppCompatActivity() {
    private var mParam1: String? = null
    private var mParam2: String? = null

    // 视图控件
    private var time: TextView? = null
    private var grid: TextView? = null
    private var schulte: RecyclerView? = null
    private var pause: TextView? = null
    private var start: TextView? = null

    // 核心逻辑对象
    private var timer: TimerHelper? = null
    private var engine: SchulteGridEngine? = null
    private var adapter: SchulteGridRVAdapter? = null
    private val viewModel: SchulteGameViewModel by viewModels<SchulteGameViewModel>()
    private val mainViewModel: MainViewModel by viewModels<MainViewModel>()
    private var ms = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置布局（复用原Fragment的布局文件）
        setContentView(R.layout.fragment_schulte_grid)

        // 获取参数（替代Fragment的getArguments）
        if (getIntent() != null && getIntent().getExtras() != null) {
            mParam1 = getIntent().getStringExtra(ARG_PARAM1)
            mParam2 = getIntent().getStringExtra(ARG_PARAM2)
        }
        // 绑定视图
        bindView()

        // 初始化计时器
        initTimer()

        // 初始化游戏引擎和RecyclerView
        initGameEngine()

        // 绑定点击事件
        bindClickListener()
    }

    /**
     * 绑定视图控件（Activity直接findViewById）
     */
    private fun bindView() {
        time = findViewById<TextView?>(R.id.time)
        grid = findViewById<TextView?>(R.id.grid)
        schulte = findViewById<RecyclerView?>(R.id.schulte)
        pause = findViewById<TextView?>(R.id.pause)
        start = findViewById<TextView?>(R.id.start)
    }

    /**
     * 初始化计时器
     */
    private fun initTimer() {
        timer = TimerHelper()
        timer!!.setOnTimerListener(OnTimerListener { `val`: Long ->
            ms = `val`
            val second = Math.toIntExact(`val` / 1000)
            time!!.setText(second.toString())
        })
    }

    /**
     * 初始化游戏引擎和RecyclerView
     */
    private fun initGameEngine() {
        engine = SchulteGridEngine()

        // 初始化单元格数据
        val list: MutableList<SchulteGridCell?> = ArrayList<SchulteGridCell?>()
        for (i in 1..engine!!.getEnd()) {
            list.add(SchulteGridCell(i))
        }

        // 设置RecyclerView适配器
        adapter = SchulteGridRVAdapter(list)
        schulte!!.setAdapter(adapter)

        // Item点击事件（核心游戏逻辑）
        adapter!!.setOnItemClickListener(OnItemClickListener { pos: Int ->
            val res = engine!!.click(adapter!!.getList().get(pos).getNum())
            if (res == -1) { // 点击错误
            } else if (res == 1) { // 游戏完成
                timer!!.stop()
                engine!!.stop()
                start!!.setText("")
                pause!!.setText("开始")
                showFinishedDialog()
                Log.d(TAG, "initGameEngine: 要保存并更新舒尔特成绩了")
                viewModel!!.saveGameTime(if (engine!!.isFourSquared()) 4 else 5, ms)
                mainViewModel.notifyRecordChanged()
            } else if (res == 0) { // 点击正确，继续游戏
                grid!!.setText(engine!!.getCur().toString() + " / " + engine!!.getEnd())
            }
        })

        // 设置网格布局（Activity中直接用this作为Context）
        val span = if (engine!!.isFourSquared()) 4 else 5
        schulte!!.setLayoutManager(GridLayoutManager(this, span))
    }

    /**
     * 绑定按钮点击事件
     */
    private fun bindClickListener() {
        // 暂停/继续按钮
        pause!!.setOnClickListener(View.OnClickListener { v: View? ->
            if (engine!!.getState() == SchulteGridEngine.State.RUNNING) {
                timer!!.pause()
                engine!!.pause()
                pause!!.setText("继续")
            } else if (engine!!.getState() == SchulteGridEngine.State.PAUSED) {
                timer!!.resume()
                engine!!.resume()
                pause!!.setText("暂停")
            }
        })

        // 开始/结束按钮
        start!!.setOnClickListener(View.OnClickListener { v: View? ->
            if (engine!!.getState() == SchulteGridEngine.State.STOPPED) {
                timer!!.start()
                engine!!.start()
                adapter!!.shuffle()
                start!!.setText("结束")
                pause!!.setText("暂停")
            } else {
                // 重置游戏状态
                timer!!.stop()
                engine!!.stop()
                start!!.setText("开始")
                pause!!.setText("暂停")

                time!!.setText("0")
                grid!!.setText("0 / " + engine!!.getEnd())

                // 重置单元格选中状态
                for (cell in adapter!!.getList()) {
                    cell.setSelected(false)
                }
                adapter!!.notifyDataSetChanged()
            }
        })
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
                "确定",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    // 重置显示
                    time!!.setText("0")
                    grid!!.setText("0 / " + engine!!.getEnd())
                    dialog!!.dismiss()
                })
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
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        // 静态创建方法（用于传递参数）
        fun newInstance(param1: String?, param2: String?): SchulteGridActivity {
            val activity = SchulteGridActivity()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            activity.getIntent().putExtras(args) // Activity用Intent传递参数
            return activity
        }
    }
}
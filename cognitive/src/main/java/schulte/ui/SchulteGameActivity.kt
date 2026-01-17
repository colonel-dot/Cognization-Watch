package schulte.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import schulte.vm.GameState
import schulte.vm.SchulteGameViewModel


class SchulteGameActivity : AppCompatActivity() {

    private val viewModel: SchulteGameViewModel by viewModels()

    private lateinit var tvTime: TextView
    private lateinit var tvNext: TextView
    private lateinit var rvGrid: RecyclerView
    private lateinit var btnReset: Button
    private lateinit var btn25: Button
    private lateinit var btn16: Button
    private var tmpSize = 5

    private lateinit var adapter: SchulteGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schulte_game)

        tvTime = findViewById(R.id.tvTime)
        tvNext = findViewById(R.id.tvNext)
        rvGrid = findViewById(R.id.rvGrid)
        btnReset = findViewById(R.id.btnReset)
        btn16 = findViewById(R.id.btn16)
        btn25 = findViewById(R.id.btn25)
        adapter = SchulteGridAdapter(5, emptyList()) { number ->
            viewModel.onCellClicked(number)
        }
        btn16.setOnClickListener { changeGridSize(4); tmpSize = 4 }
        btn25.setOnClickListener { changeGridSize(5); tmpSize = 5 }
        rvGrid.layoutManager = GridLayoutManager(this, 5)
        rvGrid.adapter = adapter

        observeViewModel()

        btnReset.setOnClickListener {
            viewModel.resetGame()
        }
    }

    private fun observeViewModel() {
        viewModel.cells.observe(this) { list ->
            adapter.submitList(list)
        }

        viewModel.nextNumber.observe(this) { next ->
            tvNext.text = "下一步：$next"
        }

        viewModel.elapsedTime.observe(this) { ms ->
            val seconds = ms / 1000.0
            tvTime.text = String.format("时间：%.3f s", seconds)
        }

        viewModel.gameState.observe(this) { state ->
            if (state == GameState.FINISHED) {
                showFinishedDialog()
            }
        }
    }

    private fun changeGridSize(size: Int) {
        if (tmpSize == size) return
        val layoutManager = rvGrid.layoutManager as GridLayoutManager
        layoutManager.spanCount = size
        adapter.updateSpanCount(size)
        when (size) {
            4 -> {
                val colorAnim4: ValueAnimator? = ObjectAnimator.ofArgb(btn16, "backgroundColor",Color.parseColor("#E0F7FA"), // 起始色（0xFFE0F7FA）
                    Color.parseColor("#2196F3")).setDuration(1000)
                val colorAnim5: ValueAnimator? = ObjectAnimator.ofArgb(btn25, "backgroundColor",Color.parseColor("#2196F3"), // 起始色（0xFFE0F7FA）
                    Color.parseColor("#E0F7FA")).setDuration(1000)
                colorAnim5?.start()
                colorAnim4?.start()
            }
            5 -> {
                val colorAnim4: ValueAnimator? = ObjectAnimator.ofArgb(btn16, "backgroundColor",Color.parseColor("#2196F3"), // 起始色（0xFFE0F7FA）
                    Color.parseColor("#E0F7FA")).setDuration(1000)
                val colorAnim5: ValueAnimator? = ObjectAnimator.ofArgb(btn25, "backgroundColor",Color.parseColor("#E0F7FA"), // 起始色（0xFFE0F7FA）
                    Color.parseColor("#2196F3")).setDuration(1000)
                colorAnim5?.start()
                colorAnim4?.start()
            }
        }
        viewModel.changeGridSize(size)
    }

    private fun showFinishedDialog() {
        val timeMs = viewModel.elapsedTime.value ?: 0L
        val seconds = timeMs / 1000.0

        AlertDialog.Builder(this)
            .setTitle("完成！")
            .setMessage(String.format("用时：%.3f 秒", seconds))
            .setPositiveButton("确定", null)
            .show()
    }
}

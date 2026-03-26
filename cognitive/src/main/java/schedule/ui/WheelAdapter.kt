package schedule.ui

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R

class WheelAdapter(val origin: List<String>) :
    RecyclerView.Adapter<WheelAdapter.VH>() {

    private val loopMultiplier = 200 // 调小后滑动在短时间内无法停止
    val data = List(origin.size * loopMultiplier) {
        origin[it % origin.size]
    }

    var selectedPos = origin.size * loopMultiplier / 2 // 初始选中中间

    inner class VH(val tv: TextView) : RecyclerView.ViewHolder(tv)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        var font: Typeface? = ResourcesCompat.getFont(parent.context, R.font.harmonyos_sans_bold)
        val tv = TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                120
            )
            gravity = Gravity.CENTER
            textSize = 18f
            typeface = font
        }
        return VH(tv)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tv.text = data[position]


        holder.tv.setTextColor(if (position == selectedPos) Color.BLACK else Color.GRAY)
        holder.tv.textSize = if (position == selectedPos) 22f else 18f
        //holder.tv.setBackgroundColor(if (position == selectedPos) Color.parseColor("#E3F2FD") else Color.TRANSPARENT)
    }

    fun getRealValue(): String {
        val realPos = getRealPosition(selectedPos)
        return origin[realPos]
    }

    fun getMiddlePosition(): Int {
        return origin.size * loopMultiplier / 2
    }

    // 新增方法：计算真实位置
    fun getRealPosition(loopPosition: Int): Int {
        return (loopPosition % origin.size + origin.size) % origin.size
    }
}
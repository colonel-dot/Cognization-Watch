package schedule.ui

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WheelAdapter(private val origin: List<String>) :
    RecyclerView.Adapter<WheelAdapter.VH>() {

    private val loopMultiplier = 200
    val data = List(origin.size * loopMultiplier) {
        origin[it % origin.size]
    }

    var selectedPos = origin.size * loopMultiplier / 2 // 初始选中中间

    inner class VH(val tv: TextView) : RecyclerView.ViewHolder(tv)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                120
            )
            gravity = Gravity.CENTER
            textSize = 18f
        }
        return VH(tv)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tv.text = data[position]
        holder.tv.setTextColor(if (position == selectedPos) Color.BLACK else Color.GRAY)
        holder.tv.textSize = if (position == selectedPos) 22f else 18f
    }

    fun getRealValue(): String {
        return origin[selectedPos % origin.size]
    }

    fun getMiddlePosition(): Int {
        return origin.size * loopMultiplier / 2
    }
}

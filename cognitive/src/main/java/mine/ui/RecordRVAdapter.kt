package mine.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import persistense.DailyBehaviorEntity

class RecordRVAdapter(val list: MutableList<DailyBehaviorEntity>): RecyclerView.Adapter<RecordRVAdapter.VH>() {


    inner class VH(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvRead = itemView.findViewById<android.widget.TextView>(R.id.record_read)
        val tvSchulte = itemView.findViewById<android.widget.TextView>(R.id.record_schulte)
        val tvSteps = itemView.findViewById<android.widget.TextView>(R.id.record_steps)
        val tvDate = itemView.findViewById<android.widget.TextView>(R.id.record_date)
        val tvSchedule = itemView.findViewById<android.widget.TextView>(R.id.record_schedule)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.record_item, parent, false) // 第三个参数必须是false，标准！
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.tvDate.text = "${item.date}"
        holder.tvRead.text = "语音评分:${item.speechScore ?: "暂无"}"
        holder.tvSchulte.text = "舒尔特成绩:${item.schulte16TimeSec?.let { "${it/1000}秒(4×4)" } ?: "暂无"} ${item.schulte25TimeSec?.let { "${it/1000}秒(5×5)" } ?: "暂无"}"
        holder.tvSteps.text = "步数:${item.steps ?: "暂无"}"

        // ==========  核心修复：格式化 总分钟数 → 时:分 格式 ==========
        val wakeTimeStr = item.wakeMinute?.let { minuteOfDay ->
            val hour = minuteOfDay / 60
            val minute = minuteOfDay % 60
            String.format("%02d:%02d", hour, minute) // 补零格式化，比如8→08，5→05
        } ?: "暂无"

        val sleepTimeStr = item.sleepMinute?.let { minuteOfDay ->
            val hour = minuteOfDay / 60
            val minute = minuteOfDay % 60
            String.format("%02d:%02d", hour, minute)
        } ?: "暂无"

        holder.tvSchedule.text = "起床:$wakeTimeStr  睡觉:$sleepTimeStr"
    }

    override fun getItemCount(): Int {
        return list.size
    }


    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, record: DailyBehaviorEntity)
    }

}
package schulte.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import schulte.data.SchulteCell

class SchulteGridAdapter(
    private var spanCount: Int,
    private var items: List<SchulteCell>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<SchulteGridAdapter.CellViewHolder>() {

    private var recyclerViewWidth = 0

    inner class CellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnNumber: Button = itemView.findViewById(R.id.btnNumber)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        // ⭐ RecyclerView 一定已存在，这里拿宽度是安全的
        recyclerView.post {
            recyclerViewWidth = recyclerView.measuredWidth
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schulte_cell, parent, false)
        return CellViewHolder(view)
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        val cell = items[position]

        if (recyclerViewWidth > 0) {
            val itemSize = (recyclerViewWidth / spanCount) - 3
            val lp = holder.itemView.layoutParams
            if (lp.width != itemSize || lp.height != itemSize) {
                lp.width = itemSize
                lp.height = itemSize
                holder.itemView.layoutParams = lp
            }
        }

        holder.btnNumber.text = cell.number.toString()
        holder.btnNumber.isEnabled = cell.enabled
        holder.btnNumber.setOnClickListener {
            onClick(cell.number)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateSpanCount(newSpanCount: Int) {
        spanCount = newSpanCount
        notifyDataSetChanged()
    }

    fun submitList(newItems: List<SchulteCell>) {
        items = newItems
        notifyDataSetChanged()
    }
}

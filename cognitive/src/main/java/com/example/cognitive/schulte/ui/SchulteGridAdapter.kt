package com.example.cognitive.schulte.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import com.example.cognitive.schulte.data.SchulteCell

class SchulteGridAdapter(
    private var items: List<SchulteCell>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<SchulteGridAdapter.CellViewHolder>() {

    inner class CellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnNumber: Button = itemView.findViewById(R.id.btnNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schulte_cell, parent, false)
        return CellViewHolder(view)
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        val cell = items[position]
        holder.btnNumber.text = cell.number.toString()
        holder.btnNumber.isEnabled = cell.enabled

        holder.btnNumber.setOnClickListener {
            onClick(cell.number)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<SchulteCell>) {
        items = newItems
        notifyDataSetChanged()
    }
}
package com.wngrlhnn.adultvideoplayer

import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VideoAdapter(private val items: List<VideoEntry>, private val onClick: (Int) -> Unit) : RecyclerView.Adapter<VideoAdapter.VH>() {
    class VH(val text: TextView) : RecyclerView.ViewHolder(text)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val text = TextView(parent.context).apply {
            setTextColor(Color.WHITE)
            textSize = 15f
            setPadding(18, 18, 18, 18)
            isSingleLine = true
        }
        return VH(text)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.text.text = items[position].title
        holder.text.setOnClickListener { onClick(position) }
    }
    override fun getItemCount() = items.size
}
package com.wngrlhnn.adultvideoplayer

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VideoAdapter(private val items: List<android.net.Uri>, private val onClick: (Int) -> Unit) : RecyclerView.Adapter<VideoAdapter.VH>() {
    class VH(val text: TextView) : RecyclerView.ViewHolder(text)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val text = TextView(parent.context).apply {
            setTextColor(android.graphics.Color.WHITE)
            textSize = 15f
            setPadding(18, 18, 18, 18)
            isSingleLine = true
        }
        return VH(text)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.text.text = "סרטון ${position + 1}"
        holder.text.setOnClickListener { onClick(position) }
    }
    override fun getItemCount() = items.size
}

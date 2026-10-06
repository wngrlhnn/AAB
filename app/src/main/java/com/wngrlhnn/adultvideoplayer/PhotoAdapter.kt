package com.wngrlhnn.adultvideoplayer

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PhotoAdapter(
    private val urls: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.VH>() {
    class VH(val image: ImageView) : RecyclerView.ViewHolder(image)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val image = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(180, 220)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(4, 4, 4, 4)
            isClickable = true
            isFocusable = true
        }
        return VH(image)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val url = urls[position]
        Glide.with(holder.image)
            .load(url)
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
            .into(holder.image)
        holder.image.setOnClickListener { onClick(url) }
    }

    override fun getItemCount() = urls.size
}
